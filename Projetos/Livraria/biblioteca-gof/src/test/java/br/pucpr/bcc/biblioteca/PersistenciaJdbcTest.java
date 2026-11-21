package br.pucpr.bcc.biblioteca;

import br.pucpr.bcc.biblioteca.dominio.*;
import br.pucpr.bcc.biblioteca.persistencia.*;
import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PersistenciaJdbcTest {
    MysqlPersistenciaFactory f;
    Connection conexao;
    @BeforeEach void preparar() throws Exception {
        f = new MysqlPersistenciaFactory("jdbc:h2:mem:"+UUID.randomUUID()+";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        conexao = ((MysqlPersistenciaFactory.MysqlUnidadeTrabalho)f.unidadeTrabalho()).getConnection();
        String schema = Files.readString(Path.of("schema.sql"));
        try (Statement s=conexao.createStatement()) {
            for(String sql : schema.split(";")) {
                String comando=sql.trim();
                if (!comando.isEmpty() && !comando.startsWith("CREATE DATABASE") && !comando.startsWith("USE ")) s.execute(comando);
            }
        }
        cadastrarVinculos();
    }
    void cadastrarVinculos() {
        f.grupos().inserirPessoa(new Pessoa("Ana","Silva","111"));
        f.grupos().inserirEstudante(new Estudante(101,"111","M101"));
        f.acervoDeLivros().inserirLivro(new Livro(1,"GoF"));
    }
    @AfterEach void fechar() throws Exception { conexao.close(); }
    @Test void R08_renovacaoAtualizaLinhaEDevolucaoEncerraEmprestimo() throws Exception {
        LocalDate inicio=LocalDate.of(2026,11,1);
        Emprestimo emp=new Emprestimo(1,101,inicio,inicio.plusDays(7));
        f.movimentacao().inserirEmprestimo(emp);
        emp=f.movimentacao().consultarEmprestimo(1,101).orElseThrow();
        emp.estenderVencimento(30); f.movimentacao().inserirEmprestimo(emp);
        Emprestimo rec=f.movimentacao().consultarEmprestimo(1,101).orElseThrow();
        assertEquals("RENOVADO",rec.getEstado());
        assertEquals(inicio.plusDays(37),rec.getDataVencimento());
        rec.marcarDevolvido(); f.movimentacao().inserirEmprestimo(rec);
        assertTrue(f.movimentacao().consultarEmprestimo(1,101).isEmpty());
        try(Statement s=conexao.createStatement(); ResultSet r=s.executeQuery("SELECT COUNT(*) FROM emprestimos")) { r.next(); assertEquals(1,r.getInt(1)); }
    }
    @Test void R01_R08_reconstituiTodosEstadosEListagemCompleta() {
        int id=0;
        for(String estado : new String[]{"LIVRE","RESERVADO","EMPRESTADO","RENOVADO","DEVOLVIDO","REPARANDO","INCINERADO"}) {
            Livro l=new Livro(++id,"Estado "+estado); l.reconstituirEstadoPorNome(estado);
            l.setNumRenovacoes(2); l.setIdReservadoParaEstudante(101); l.marcarExcluido();
            f.acervoDeLivros().inserirLivro(l);
            Livro rec=f.acervoDeLivros().consultarLivro(id).orElseThrow();
            assertEquals(estado,rec.getEstadoNome()); assertEquals(2,rec.getNumRenovacoes());
            assertEquals(101,rec.getIdReservadoParaEstudante()); assertTrue(rec.isExcluido());
        }
        assertEquals(7,f.acervoDeLivros().listarLivros().size());
        for(Livro l:f.acervoDeLivros().listarLivros()) {
            assertEquals(101,l.getIdReservadoParaEstudante()); assertTrue(l.isExcluido());
        }
    }
    @Test void R08_rollbackJdbcDesfazEscritaECommitPersiste() {
        f.unidadeTrabalho().iniciar(); f.acervoDeLivros().inserirLivro(new Livro(20,"rollback"));
        f.unidadeTrabalho().rollback(); assertTrue(f.acervoDeLivros().consultarLivro(20).isEmpty());
        f.unidadeTrabalho().iniciar(); f.acervoDeLivros().inserirLivro(new Livro(21,"commit"));
        f.unidadeTrabalho().commit(); assertTrue(f.acervoDeLivros().consultarLivro(21).isPresent());
        assertFalse(f.unidadeTrabalho().isAtiva());
    }
    @Test void R07_R08_atualizarPessoaReferenciadaNaoExecutaDeleteImplicito() {
        f.grupos().inserirPessoa(new Pessoa("Ana","Silva","111"));
        f.grupos().inserirEstudante(new Estudante(101,"111","M101"));
        assertDoesNotThrow(() -> f.grupos().inserirPessoa(new Pessoa("Ana Maria","Silva","111")));
        assertEquals("Ana Maria",f.grupos().consultarPessoa("111").orElseThrow().nome());
        assertTrue(f.grupos().consultarEstudante(101).isPresent());
    }

    @Test void R08_falhaCommitPermiteRollbackERetentativa() throws Exception {
        var falhar = new java.util.concurrent.atomic.AtomicBoolean(true);
        Connection proxy=(Connection)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class[]{Connection.class},(obj,method,args)-> {
                if(method.getName().equals("commit") && falhar.getAndSet(false)) throw new SQLException("commit indisponível");
                try { return method.invoke(conexao,args); }
                catch(java.lang.reflect.InvocationTargetException e) { throw e.getCause(); }
            });
        var unidade=new MysqlPersistenciaFactory.MysqlUnidadeTrabalho("", "", "") {
            @Override public Connection getConnection() { return proxy; }
        };
        var livros=new MysqlPersistenciaFactory.MysqlAcervoDeLivros(unidade);
        unidade.iniciar(); livros.inserirLivro(new Livro(9,"não confirmar"));
        assertThrows(IllegalStateException.class,unidade::commit);
        assertTrue(unidade.isAtiva()); unidade.rollback();
        assertFalse(unidade.isAtiva()); assertTrue(livros.consultarLivro(9).isEmpty());
        unidade.iniciar(); livros.inserirLivro(new Livro(10,"confirmar")); unidade.commit();
        assertTrue(livros.consultarLivro(10).isPresent());
    }
    @Test void R08_conexaoInvalidaNaoMarcaTransacaoAtiva() {
        try(var invalida=new MysqlPersistenciaFactory("jdbc:inexistente:teste","","")) {
            assertThrows(IllegalStateException.class,()->invalida.unidadeTrabalho().iniciar());
            assertFalse(invalida.unidadeTrabalho().isAtiva());
        }
    }
    @Test void R08_integridadeReferencialImpedeOrfaos() {
        assertThrows(IllegalStateException.class,()->f.movimentacao().inserirReserva(new Reserva(99,101,LocalDate.now())));
        f.movimentacao().inserirReserva(new Reserva(1,101,LocalDate.now()));
        assertThrows(IllegalStateException.class,()->f.grupos().excluirEstudante(101));
        assertThrows(IllegalStateException.class,()->f.acervoDeLivros().excluirLivro(1));
        assertThrows(IllegalStateException.class,()->f.grupos().excluirPessoa("111"));
    }
}
