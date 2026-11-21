package br.pucpr.bcc.biblioteca;

import br.pucpr.bcc.biblioteca.dominio.*;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.*;
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
        f.repositorioRelacionamento().salvarPessoa(new Pessoa("Ana","Silva","111"));
        f.repositorioRelacionamento().salvarEstudante(new Estudante(101,"111","M101"));
        f.repositorioLivro().salvar(new Livro(1,"GoF"));
    }
    @AfterEach void fechar() throws Exception { conexao.close(); }
    @Test void R08_renovacaoAtualizaLinhaEDevolucaoEncerraEmprestimo() throws Exception {
        LocalDate inicio=LocalDate.of(2026,11,1);
        Emprestimo emp=new Emprestimo(1,101,inicio,inicio.plusDays(7));
        f.repositorioMovimentacao().salvarEmprestimo(emp);
        emp=f.repositorioMovimentacao().buscarEmprestimoAtivo(1,101).orElseThrow();
        emp.estenderVencimento(30); f.repositorioMovimentacao().salvarEmprestimo(emp);
        Emprestimo rec=f.repositorioMovimentacao().buscarEmprestimoAtivo(1,101).orElseThrow();
        assertEquals("RENOVADO",rec.getEstado());
        assertEquals(inicio.plusDays(37),rec.getDataVencimento());
        rec.marcarDevolvido(); f.repositorioMovimentacao().salvarEmprestimo(rec);
        assertTrue(f.repositorioMovimentacao().buscarEmprestimoAtivo(1,101).isEmpty());
        try(Statement s=conexao.createStatement(); ResultSet r=s.executeQuery("SELECT COUNT(*) FROM emprestimos")) { r.next(); assertEquals(1,r.getInt(1)); }
    }
    @Test void R01_R08_reconstituiTodosEstadosEListagemCompleta() {
        int id=0;
        for(String estado : new String[]{"LIVRE","RESERVADO","EMPRESTADO","RENOVADO","DEVOLVIDO","REPARANDO","INCINERADO"}) {
            Livro l=new Livro(++id,"Estado "+estado); l.reconstituirEstadoPorNome(estado);
            l.setNumRenovacoes(2); l.setIdReservadoParaEstudante(101); l.marcarExcluido();
            f.repositorioLivro().salvar(l);
            Livro rec=f.repositorioLivro().buscarPorId(id).orElseThrow();
            assertEquals(estado,rec.getEstadoNome()); assertEquals(2,rec.getNumRenovacoes());
            assertEquals(101,rec.getIdReservadoParaEstudante()); assertTrue(rec.isExcluido());
        }
        assertEquals(7,f.repositorioLivro().listarTodos().size());
        for(Livro l:f.repositorioLivro().listarTodos()) {
            assertEquals(101,l.getIdReservadoParaEstudante()); assertTrue(l.isExcluido());
        }
    }
    @Test void R08_rollbackJdbcDesfazEscritaECommitPersiste() {
        f.unidadeTrabalho().iniciar(); f.repositorioLivro().salvar(new Livro(20,"rollback"));
        f.unidadeTrabalho().rollback(); assertTrue(f.repositorioLivro().buscarPorId(20).isEmpty());
        f.unidadeTrabalho().iniciar(); f.repositorioLivro().salvar(new Livro(21,"commit"));
        f.unidadeTrabalho().commit(); assertTrue(f.repositorioLivro().buscarPorId(21).isPresent());
        assertFalse(f.unidadeTrabalho().isAtiva());
    }
    @Test void R07_R08_atualizarPessoaReferenciadaNaoExecutaDeleteImplicito() {
        f.repositorioRelacionamento().salvarPessoa(new Pessoa("Ana","Silva","111"));
        f.repositorioRelacionamento().salvarEstudante(new Estudante(101,"111","M101"));
        assertDoesNotThrow(() -> f.repositorioRelacionamento().salvarPessoa(new Pessoa("Ana Maria","Silva","111")));
        assertEquals("Ana Maria",f.repositorioRelacionamento().buscarPessoaPorCpf("111").orElseThrow().nome());
        assertTrue(f.repositorioRelacionamento().buscarEstudantePorId(101).isPresent());
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
        var livros=new MysqlPersistenciaFactory.MysqlRepositorioLivro(unidade);
        unidade.iniciar(); livros.salvar(new Livro(9,"não confirmar"));
        assertThrows(IllegalStateException.class,unidade::commit);
        assertTrue(unidade.isAtiva()); unidade.rollback();
        assertFalse(unidade.isAtiva()); assertTrue(livros.buscarPorId(9).isEmpty());
        unidade.iniciar(); livros.salvar(new Livro(10,"confirmar")); unidade.commit();
        assertTrue(livros.buscarPorId(10).isPresent());
    }
    @Test void R08_conexaoInvalidaNaoMarcaTransacaoAtiva() {
        try(var invalida=new MysqlPersistenciaFactory("jdbc:inexistente:teste","","")) {
            assertThrows(IllegalStateException.class,()->invalida.unidadeTrabalho().iniciar());
            assertFalse(invalida.unidadeTrabalho().isAtiva());
        }
    }
    @Test void R08_integridadeReferencialImpedeOrfaos() {
        assertThrows(IllegalStateException.class,()->f.repositorioMovimentacao().salvarReserva(new Reserva(99,101,LocalDate.now())));
        f.repositorioMovimentacao().salvarReserva(new Reserva(1,101,LocalDate.now()));
        assertThrows(IllegalStateException.class,()->f.repositorioRelacionamento().removerEstudante(101));
        assertThrows(IllegalStateException.class,()->f.repositorioLivro().remover(1));
        assertThrows(IllegalStateException.class,()->f.repositorioRelacionamento().removerPessoa("111"));
    }
}
