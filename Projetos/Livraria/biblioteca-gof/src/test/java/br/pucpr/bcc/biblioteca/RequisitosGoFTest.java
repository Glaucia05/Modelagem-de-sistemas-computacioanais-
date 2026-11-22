package br.pucpr.bcc.biblioteca;

import br.pucpr.bcc.biblioteca.dominio.*;
import br.pucpr.bcc.biblioteca.interfaces.*;
import br.pucpr.bcc.biblioteca.r02_strategy.*;
import br.pucpr.bcc.biblioteca.r03_adapter.*;
import br.pucpr.bcc.biblioteca.r04_facade.CirculacaoFacade;
import br.pucpr.bcc.biblioteca.r04_facade.ComprovanteOperacao;
import br.pucpr.bcc.biblioteca.r05_observer.*;
import br.pucpr.bcc.biblioteca.r06_command.ExecutorComando;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.*;
import br.pucpr.bcc.biblioteca.r09_decorator.*;
import br.pucpr.bcc.biblioteca.r10_composite.*;
import br.pucpr.bcc.biblioteca.tratadores.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RequisitosGoFTest {
    private PersistenciaFactory factory;
    private ConfiguracaoMulta configMulta;
    private BancoASimulado bancoA;
    private BancoBSimulado bancoB;
    private BancoAAdapter adapterA;
    private BancoBAdapter adapterB;
    private PublicadorDevolucao publicador;
    private CirculacaoFacade facade;
    private TratadorDeCadastro cadastro;
    private iEmprestimo emprestimo;
    private iDevolucao devolucao;

    @BeforeEach
    void setup() {
        factory = ProvedorPersistencia.criarPorPerfil("test");
        configMulta = new ConfiguracaoMulta(new MultaOriginalStrategy());
        bancoA = new BancoASimulado();
        bancoB = new BancoBSimulado();
        adapterA = new BancoAAdapter(bancoA);
        adapterB = new BancoBAdapter(bancoB);
        publicador = new PublicadorDevolucao();
        facade = new CirculacaoFacade(
                factory.unidadeTrabalho(),
                factory.acervoDeLivros(),
                factory.movimentacao(),
                factory.relacionamento(),
                publicador
        );
        cadastro = new TratadorDeCadastro(factory.acervoDeLivros(), factory.relacionamento());
        emprestimo = new TratadorDeEmprestimo(facade, new ExecutorComando());
        devolucao = new TratadorDeDevolucao(
                facade,
                factory.movimentacao(),
                factory.relacionamento(),
                configMulta,
                adapterA
        );

        cadastro.inserirPessoa("Ana", "Silva", "11122233344");
        cadastro.inserirEstudante("11122233344", 101, "MAT-101");
        cadastro.inserirLivro(1, "Clean Code");
    }

    @Test
    void R01_terceiraRenovacaoRecusadaEDevolvidoNaoInspercionadoNaoReserva() {
        LocalDate base = LocalDate.of(2026, 11, 1);
        assertTrue(emprestimo.solicitarReserva("C1", "user", 1, 101, base).sucesso());
        assertTrue(emprestimo.efetivarEmprestimo(1, 101, base, 7).sucesso());

        assertTrue(emprestimo.renovarEmprestimo("C2", "user", 1, 101).sucesso());
        LocalDate vencApos1a = factory.movimentacao().consultarEmprestimo(1, 101).orElseThrow().getDataVencimento();
        assertEquals(base.plusDays(37), vencApos1a);

        assertTrue(emprestimo.renovarEmprestimo("C3", "user", 1, 101).sucesso());
        LocalDate vencApos2a = factory.movimentacao().consultarEmprestimo(1, 101).orElseThrow().getDataVencimento();
        assertEquals(base.plusDays(67), vencApos2a);

        ComprovanteOperacao r3 = emprestimo.renovarEmprestimo("C4", "user", 1, 101);
        assertFalse(r3.sucesso());
        assertEquals(vencApos2a, factory.movimentacao().consultarEmprestimo(1, 101).orElseThrow().getDataVencimento());

        assertTrue(devolucao.registrarDevolucao(1, 101, base.plusDays(70)).sucesso());
        assertEquals("DEVOLVIDO", factory.acervoDeLivros().consultarLivro(1).orElseThrow().getEstadoNome());
        assertFalse(emprestimo.solicitarReserva("C5", "user", 1, 101, base.plusDays(71)).sucesso());

        // Inspeção danificado -> REPARANDO -> laudo irrecuperável -> INCINERADO
        assertTrue(devolucao.inspecionarLivro(1, false).sucesso());
        assertEquals("REPARANDO", factory.acervoDeLivros().consultarLivro(1).orElseThrow().getEstadoNome());
        assertTrue(devolucao.emitirLaudoIrrecuperavel(1).sucesso());
        assertEquals("INCINERADO", factory.acervoDeLivros().consultarLivro(1).orElseThrow().getEstadoNome());
    }

    @Test
    void R02_politicasMultaCincoDiasESemAtraso() {
        LocalDate venc = LocalDate.of(2026, 11, 10);
        PoliticaMulta original = new MultaOriginalStrategy();
        PoliticaMulta tolerancia = new MultaToleranciaDoisDiasStrategy();
        PoliticaMulta teto = new MultaTetoVinteStrategy();

        assertEquals(new BigDecimal("5.00"), original.calcular(venc, venc.plusDays(5)));
        assertEquals(new BigDecimal("3.00"), tolerancia.calcular(venc, venc.plusDays(5)));
        assertEquals(new BigDecimal("20.00"), teto.calcular(venc, venc.plusDays(30)));
        assertEquals(new BigDecimal("0.00"), original.calcular(venc, venc));
    }

    @Test
    void R03_adaptadorBancosAebESemDuplaQuitacao() {
        BigDecimal seteReais = new BigDecimal("7.00");
        String idA = adapterA.registrarCobranca("REF-1", "111.222.333-44", seteReais);
        String idB = adapterB.registrarCobranca("REF-1", "111.222.333-44", seteReais);

        assertEquals(700L, bancoA.getValorCentavos(idA));
        assertEquals(7.00, bancoB.getMontanteReais(idB), 0.0001);

        var primeira = adapterA.processarNotificacao(new BancoASimulado.NotificacaoBancoA(idA, 700L, "LIQUIDADO"));
        var repetida = adapterA.processarNotificacao(new BancoASimulado.NotificacaoBancoA(idA, 700L, "LIQUIDADO"));

        assertTrue(primeira.isPresent());
        assertEquals("QUITADO", primeira.get().situacaoPadronizada());
        assertTrue(repetida.isEmpty());
    }

    @Test
    void R04_fachadaEfetivaEmprestimoRemovendoReservaSemExporColecoes() {
        LocalDate hoje = LocalDate.of(2026, 11, 10);
        assertTrue(emprestimo.solicitarReserva("R-1", "user", 1, 101, hoje).sucesso());

        ComprovanteOperacao comp = facade.efetivarEmprestimo(1, 101, hoje, 14);
        assertTrue(comp.sucesso());
        assertEquals("EMPRESTADO", comp.estadoLivroResultante());
        assertTrue(factory.movimentacao().consultarReserva(1, 101).isEmpty());
        assertTrue(factory.movimentacao().consultarEmprestimo(1, 101).isPresent());
    }

    @Test
    void R05_observadoresDevolucaoAtrasadaENoPrazo() {
        OuvinteInspecao inspecao = new OuvinteInspecao();
        OuvinteMulta multa = new OuvinteMulta();
        OuvinteIndicadores indicadores = new OuvinteIndicadores();
        OuvinteEmail email = new OuvinteEmail();
        email.setSimularFalhaSmtp(true);

        publicador.inscrever(inspecao);
        publicador.inscrever(multa);
        publicador.inscrever(indicadores);
        publicador.inscrever(email);

        LocalDate inicio = LocalDate.of(2026, 11, 1);
        emprestimo.solicitarReserva("O-1", "user", 1, 101, inicio);
        emprestimo.efetivarEmprestimo(1, 101, inicio, 5);

        ComprovanteOperacao res = devolucao.registrarDevolucao(1, 101, LocalDate.of(2026, 11, 9));
        assertTrue(res.sucesso());
        assertEquals("DEVOLVIDO", factory.acervoDeLivros().consultarLivro(1).orElseThrow().getEstadoNome());
        assertEquals(1, inspecao.getFilaInspecaoLivros().size());
        assertEquals(1, multa.getDevolucoesComAtraso().size());
        assertEquals(1, indicadores.getTotalDevolucoes());
        assertEquals(1, indicadores.getTotalAtrasadas());
    }

    @Test
    void R06_comandoRenovacaoIdempotenteMesmoId() {
        LocalDate inicio = LocalDate.of(2026, 11, 1);
        emprestimo.solicitarReserva("CMD-R", "user", 1, 101, inicio);
        emprestimo.efetivarEmprestimo(1, 101, inicio, 10);

        emprestimo.renovarEmprestimo("REN-MESMO-ID", "user", 1, 101);
        emprestimo.renovarEmprestimo("REN-MESMO-ID", "user", 1, 101);

        Emprestimo emp = factory.movimentacao().consultarEmprestimo(1, 101).orElseThrow();
        assertEquals(inicio.plusDays(40), emp.getDataVencimento());
        assertEquals(1, factory.acervoDeLivros().consultarLivro(1).orElseThrow().getNumRenovacoes());
    }

    @Test
    void R07_templateMethodEstudanteDuplicadoECpfInexistente() {
        var duplicado = cadastro.inserirEstudanteComResultado(101, "11122233344", "MAT-101");
        assertTrue(duplicado.sucesso());
        assertTrue(duplicado.jaExistia());

        assertThrows(IllegalStateException.class,
                () -> cadastro.inserirEstudante("99999999999", 202, "MAT-202"));
        assertTrue(factory.relacionamento().consultarEstudante(202).isEmpty());
    }

    @Test
    void R08_abstractFactoryFamiliasMemoriaEMysql() {
        PersistenciaFactory memoria = ProvedorPersistencia.criarPorPerfil("test");
        assertTrue(memoria instanceof MemoriaPersistenciaFactory);

        PersistenciaFactory mysql = ProvedorPersistencia.criarPorPerfil("prod");
        assertTrue(mysql instanceof MysqlPersistenciaFactory);
        MysqlPersistenciaFactory mf = (MysqlPersistenciaFactory) mysql;
        assertTrue(((MysqlPersistenciaFactory.MysqlAcervoDeLivros) mf.acervoDeLivros()).usaMesmaTransacao(mf.unidadeTrabalho()));
        assertTrue(((MysqlPersistenciaFactory.MysqlMovimentacao) mf.movimentacao()).usaMesmaTransacao(mf.unidadeTrabalho()));
    }

    @Test
    void R09_decoratorCacheMetadadosMedicaoEEstadoAtualNaReserva() {
        CacheMetadadosDecorator cache = new CacheMetadadosDecorator(new ConsultaAcervoBase(factory.acervoDeLivros()));
        MedicaoConsultaDecorator medicao = new MedicaoConsultaDecorator(cache);

        assertTrue(medicao.consultarMetadados(1).isPresent());
        assertTrue(medicao.consultarMetadados(1).isPresent());
        assertEquals(2, medicao.getTotalMedicoes());

        LocalDate hoje = LocalDate.of(2026, 11, 10);
        emprestimo.solicitarReserva("DEC-1", "user", 1, 101, hoje);
        emprestimo.efetivarEmprestimo(1, 101, hoje, 7);

        assertFalse(medicao.podeReservar(1));
    }

    @Test
    void R10_compositeHierarquiaContagemEMoverSemAlterarLivro() {
        Set<Integer> globais = new HashSet<>();
        GrupoAcervoComposite unidade = new GrupoAcervoComposite("Unidade Central", globais);
        GrupoAcervoComposite secaoA = new GrupoAcervoComposite("Seção A", globais);
        GrupoAcervoComposite secaoB = new GrupoAcervoComposite("Seção B", globais);

        cadastro.inserirLivro(2, "Livro 2");
        cadastro.inserirLivro(3, "Livro 3");
        cadastro.inserirLivro(4, "Livro 4");
        cadastro.inserirLivro(5, "Livro 5");

        ItemLivroComposite i1 = new ItemLivroComposite(1, "Clean Code");
        ItemLivroComposite i2 = new ItemLivroComposite(2, "Livro 2");
        ItemLivroComposite i3 = new ItemLivroComposite(3, "Livro 3");
        ItemLivroComposite i4 = new ItemLivroComposite(4, "Livro 4");
        ItemLivroComposite i5 = new ItemLivroComposite(5, "Livro 5");

        secaoA.adicionar(i1);
        secaoA.adicionar(i2);
        secaoA.adicionar(i3);
        secaoB.adicionar(i4);
        secaoB.adicionar(i5);

        unidade.adicionar(secaoA);
        unidade.adicionar(secaoB);

        assertEquals(5, unidade.contarLivros());

        Livro antes = factory.acervoDeLivros().consultarLivro(2).orElseThrow();
        String estadoAntes = antes.getEstadoNome();
        assertTrue(secaoA.moverItemPara(i2, secaoB));
        assertEquals(5, unidade.contarLivros());
        assertEquals(2, secaoA.contarLivros());
        assertEquals(3, secaoB.contarLivros());
        assertEquals(estadoAntes, factory.acervoDeLivros().consultarLivro(2).orElseThrow().getEstadoNome());
    }
}
