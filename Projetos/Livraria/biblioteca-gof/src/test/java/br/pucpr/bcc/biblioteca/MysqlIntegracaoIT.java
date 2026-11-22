package br.pucpr.bcc.biblioteca;

import br.pucpr.bcc.biblioteca.dominio.*;
import br.pucpr.bcc.biblioteca.r02_strategy.*;
import br.pucpr.bcc.biblioteca.r03_adapter.*;
import br.pucpr.bcc.biblioteca.r04_facade.*;
import br.pucpr.bcc.biblioteca.r05_observer.*;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.*;
import br.pucpr.bcc.biblioteca.tratadores.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;
import static org.junit.jupiter.api.Assertions.*;

/** Execução explícita: mvn -Pmysql-it verify. Não apaga dados nem usa a base de produção. */
class MysqlIntegracaoIT {
    @Test void R08_mysqlRealConfirmaFluxoRollbackEstadosEIntegridade() throws Exception {
        String url = System.getenv("DB_URL");
        assertNotNull(url, "Configure DB_URL para a base biblioteca_gof_validacao");
        assertTrue(url.startsWith("jdbc:mysql:"), "Este teste exige MySQL real");
        String usuario = System.getenv("DB_USER");
        assertNotNull(usuario, "Configure DB_USER");
        try (var f = new MysqlPersistenciaFactory(url, usuario, System.getenv().getOrDefault("DB_PASS", ""))) {
            var uow = (MysqlPersistenciaFactory.MysqlUnidadeTrabalho) f.unidadeTrabalho();
            Connection c = uow.getConnection();
            assertEquals("biblioteca_gof_validacao", c.getCatalog(), "Use exclusivamente a base de validação");
            assertEquals("MySQL", c.getMetaData().getDatabaseProductName());
            System.out.println("MySQL real: " + c.getMetaData().getDatabaseProductVersion());
            // Apenas tabelas ausentes: não executa CREATE DATABASE/USE nem migra tabelas existentes.
            try (Statement st = c.createStatement()) {
                for (String trecho : Files.readString(Path.of("schema.sql")).split(";")) {
                    String sql = trecho.trim();
                    if (sql.startsWith("CREATE TABLE")) st.execute(sql);
                }
            }
            assertSame(uow, f.acervoDeLivros().unidadeTrabalho());
            assertSame(uow, f.movimentacao().unidadeTrabalho());
            assertSame(uow, f.relacionamento().unidadeTrabalho());
            int id = ThreadLocalRandom.current().nextInt(100_000_000, 900_000_000);
            for (int i = 0; i < 10; i++) assertTrue(f.acervoDeLivros().consultarLivro(id+i).isEmpty());
            String cpf = String.format("%011d", id);
            assertTrue(f.relacionamento().consultarPessoa(cpf).isEmpty());
            assertTrue(f.relacionamento().consultarEstudante(id).isEmpty());
            var cadastro = new TratadorDeCadastro(f.acervoDeLivros(), f.relacionamento());
            cadastro.inserirPessoa("Validação", "GoF", cpf);
            cadastro.inserirEstudante(cpf, id, "IT-"+id);
            cadastro.inserirLivro(id, "Validação JDBC");
            var publicador = new PublicadorDevolucao();
            var facade = new CirculacaoFacade(uow, f.acervoDeLivros(), f.movimentacao(), f.relacionamento(), publicador);
            var devolucao = new TratadorDeDevolucao(facade, f.movimentacao(), f.relacionamento(),
                    new ConfiguracaoMulta(new MultaOriginalStrategy()), new BancoAAdapter(new BancoASimulado()));
            LocalDate data = LocalDate.of(2026,11,20);
            assertTrue(facade.solicitarReserva(id,id,data).sucesso());
            assertTrue(facade.efetivarEmprestimo(id,id,data,7).sucesso());
            assertTrue(f.movimentacao().consultarReserva(id,id).isEmpty());
            assertTrue(facade.renovarEmprestimo(id,id).sucesso());
            assertTrue(facade.renovarEmprestimo(id,id).sucesso());
            assertFalse(facade.renovarEmprestimo(id,id).sucesso());
            assertEquals(data.plusDays(67),f.movimentacao().consultarEmprestimo(id,id).orElseThrow().getDataVencimento());
            assertTrue(devolucao.registrarDevolucao(id,id,data.plusDays(72)).sucesso());
            assertTrue(f.movimentacao().consultarEmprestimo(id,id).isEmpty());
            var boleto=devolucao.emitirBoletoMulta(id,id).orElseThrow();
            assertEquals(new BigDecimal("5.00"),boleto.valor());
            assertTrue(f.movimentacao().listarBoletos().stream()
                    .anyMatch(b -> b.idCobranca().equals(boleto.idCobranca()) && b.idLivro()==id && b.valor().compareTo(boleto.valor())==0));
            assertFalse(facade.solicitarReserva(id,id,data.plusDays(73)).sucesso());
            try (PreparedStatement st=c.prepareStatement("SELECT COUNT(*) FROM emprestimos WHERE id_livro=?")) {
                st.setInt(1,id); try(ResultSet rs=st.executeQuery()) {rs.next();assertEquals(1,rs.getInt(1));}
            }
            uow.iniciar();
            f.acervoDeLivros().salvar(new Livro(id+1,"Desfazer"));
            uow.rollback();
            assertTrue(f.acervoDeLivros().consultarLivro(id+1).isEmpty());
            int n=2;
            for(String estado:new String[]{"LIVRE","RESERVADO","EMPRESTADO","RENOVADO","DEVOLVIDO","REPARANDO","INCINERADO"}) {
                Livro livro=new Livro(id+n++,"Estado "+estado); livro.reconstituirEstadoPorNome(estado);
                f.acervoDeLivros().salvar(livro);
                assertEquals(estado,f.acervoDeLivros().consultarLivro(livro.getIdLivro()).orElseThrow().getEstadoNome());
            }
            assertThrows(IllegalStateException.class,()->f.relacionamento().excluirEstudante(id));
            assertThrows(IllegalStateException.class,()->f.acervoDeLivros().excluirLivro(id));
            assertThrows(IllegalStateException.class,()->f.movimentacao().inserirReserva(new Reserva(id+1,id,data)));
            // Outra conexão comprova que os commits ficaram visíveis fora da UOW original.
            try(var outra=new MysqlPersistenciaFactory(url,usuario,System.getenv().getOrDefault("DB_PASS",""))) {
                assertEquals("DEVOLVIDO",outra.acervoDeLivros().consultarLivro(id).orElseThrow().getEstadoNome());
                assertTrue(outra.movimentacao().consultarEmprestimo(id,id).isEmpty());
            }
        }
    }
}
