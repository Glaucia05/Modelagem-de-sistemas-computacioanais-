package br.pucpr.bcc.biblioteca;

import br.pucpr.bcc.biblioteca.dominio.*;
import br.pucpr.bcc.biblioteca.r02_strategy.*;
import br.pucpr.bcc.biblioteca.r03_adapter.*;
import br.pucpr.bcc.biblioteca.r04_facade.CirculacaoFacade;
import br.pucpr.bcc.biblioteca.r05_observer.PublicadorDevolucao;
import br.pucpr.bcc.biblioteca.r06_command.ExecutorComando;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.*;
import br.pucpr.bcc.biblioteca.tratadores.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica que o código usa os nomes dos diagramas UML (Diagrama de Classes de Projeto e Domínio). */
class AlinhamentoUmlTest {

    @Test
    void UML_transacoesHerdamDeTransacaoComEstadoEmprestimo() {
        LocalDate d = LocalDate.of(2026, 11, 1);
        assertTrue(Transacao.class.isAssignableFrom(Reserva.class));
        assertTrue(Transacao.class.isAssignableFrom(Emprestimo.class));
        assertTrue(Transacao.class.isAssignableFrom(Devolucao.class));
        Emprestimo e = new Emprestimo(1, 10, d, d.plusDays(7));
        assertEquals("EMPRESTADO", e.consultarEstado());
        e.renovar(30);
        assertEquals("RENOVADO", e.consultarEstado());
        assertEquals(d.plusDays(37), e.getDataVencimento());
        assertEquals(d, e.getData());
    }

    @Test
    void UML_relacionamentoReuneGruposDoModeloDeClasses() {
        PersistenciaFactory f = ProvedorPersistencia.criarPorPerfil("test");
        Relacionamento r = f.relacionamento();
        assertInstanceOf(GrupoDePessoas.class, r);
        assertInstanceOf(GrupoDeAcademicos.class, r);
        assertInstanceOf(GrupoDeColaboradores.class, r);
        assertInstanceOf(AcervoDeLivros.class, f.acervoDeLivros());
        assertInstanceOf(Movimentacao.class, f.movimentacao());
    }

    @Test
    void UML_tratadoresExpoemOperacoesDoDiagramaDeClasses() {
        PersistenciaFactory f = ProvedorPersistencia.criarPorPerfil("test");
        CirculacaoFacade facade = new CirculacaoFacade(f.unidadeTrabalho(), f.acervoDeLivros(),
                f.movimentacao(), f.relacionamento(), new PublicadorDevolucao());
        TratadorDeCadastro cadastro = new TratadorDeCadastro(f.acervoDeLivros(), f.relacionamento());
        TratadorDeEmprestimo emprestimo = new TratadorDeEmprestimo(facade, new ExecutorComando());
        TratadorDeDevolucao devolucao = new TratadorDeDevolucao(facade, f.movimentacao(), f.relacionamento(),
                new ConfiguracaoMulta(new MultaOriginalStrategy()), new BancoAAdapter(new BancoASimulado()));
        LocalDate hoje = LocalDate.of(2026, 11, 1);

        cadastro.inserirPessoa("Ana", "Silva", "11122233344");
        cadastro.inserirEstudante("11122233344", 101, "M101");
        cadastro.inserirLivro(1, "Modelagem");

        assertTrue(emprestimo.solicitarReservaLivro("C1", "ana", 1, 101, hoje).sucesso());
        assertTrue(emprestimo.consultarReservaLivro(1, 101));
        assertTrue(emprestimo.emprestarLivro(1, 101, hoje, 7).sucesso());
        assertTrue(emprestimo.renovarEmprestimoLivro("C2", "ana", 1, 101).sucesso());
        assertTrue(devolucao.registrarDevolucaoLivro(1, 101, hoje.plusDays(42)).sucesso());
        assertEquals(0, new BigDecimal("5.00").compareTo(
                devolucao.calcularMulta(hoje.plusDays(37), hoje.plusDays(42))));
        assertTrue(devolucao.emitirBoletoMulta(1, 101).isPresent());
    }
}
