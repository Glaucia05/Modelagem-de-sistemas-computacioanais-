package br.pucpr.bcc.biblioteca;

import br.pucpr.bcc.biblioteca.dominio.*;
import br.pucpr.bcc.biblioteca.r02_strategy.*;
import br.pucpr.bcc.biblioteca.r03_adapter.*;
import br.pucpr.bcc.biblioteca.r04_facade.*;
import br.pucpr.bcc.biblioteca.r05_observer.*;
import br.pucpr.bcc.biblioteca.r06_command.*;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.*;
import br.pucpr.bcc.biblioteca.r09_decorator.*;
import br.pucpr.bcc.biblioteca.r10_composite.*;
import br.pucpr.bcc.biblioteca.tratadores.*;
import org.junit.jupiter.api.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

class AuditoriaRegressaoTest {
    final LocalDate base = LocalDate.of(2026, 11, 1);
    MemoriaPersistenciaFactory f;
    PublicadorDevolucao eventos;
    CirculacaoFacade facade;
    TratadorDeCadastro cadastro;
    ConfiguracaoMulta config;
    TratadorDeDevolucao devolucao;

    @BeforeEach void preparar() {
        f = new MemoriaPersistenciaFactory();
        eventos = new PublicadorDevolucao();
        facade = new CirculacaoFacade(f.unidadeTrabalho(), f.repositorioLivro(),
                f.repositorioMovimentacao(), f.repositorioRelacionamento(), eventos);
        cadastro = new TratadorDeCadastro(f.repositorioLivro(), f.repositorioRelacionamento());
        cadastro.inserirPessoa("Ana", "Silva", "11122233344");
        cadastro.inserirEstudante("11122233344", 101, "M101");
        cadastro.inserirLivro(1, "GoF");
        config = new ConfiguracaoMulta(new MultaOriginalStrategy());
        devolucao = new TratadorDeDevolucao(facade, f.repositorioMovimentacao(),
                f.repositorioRelacionamento(), config, new BancoAAdapter(new BancoASimulado()));
    }
    void emprestar() {
        assertTrue(facade.solicitarReserva(1,101,base).sucesso());
        assertTrue(facade.efetivarEmprestimo(1,101,base,7).sucesso());
    }
    @Test void R04_rollbackRestauraLivroReservaEEmprestimoAposFalhaIntermediaria() {
        assertTrue(facade.solicitarReserva(1,101,base).sucesso());
        RepositorioLivro falho = new RepositorioLivro() {
            public void salvar(Livro l) { f.repositorioLivro().salvar(l); throw new IllegalStateException("Falha injetada"); }
            public Optional<Livro> buscarPorId(int id) { return f.repositorioLivro().buscarPorId(id); }
            public List<Livro> listarTodos() { return f.repositorioLivro().listarTodos(); }
            public void remover(int id) { f.repositorioLivro().remover(id); }
        };
        var comFalha = new CirculacaoFacade(f.unidadeTrabalho(), falho,
                f.repositorioMovimentacao(), f.repositorioRelacionamento(), eventos);
        assertFalse(comFalha.efetivarEmprestimo(1,101,base,7).sucesso());
        assertEquals("RESERVADO", f.repositorioLivro().buscarPorId(1).orElseThrow().getEstadoNome());
        assertTrue(f.repositorioMovimentacao().buscarReservaAtiva(1,101).isPresent());
        assertTrue(f.repositorioMovimentacao().buscarEmprestimoAtivo(1,101).isEmpty());
        assertFalse(f.unidadeTrabalho().isAtiva());
        assertTrue(facade.efetivarEmprestimo(1,101,base,7).sucesso());
    }
    @Test void R05_noPrazoEmailFalhaAntesDosDemaisEPublicacaoAposCommit() {
        var email = new OuvinteEmail(); email.setSimularFalhaSmtp(true);
        var inspecao = new OuvinteInspecao(); var multa = new OuvinteMulta();
        var indicadores = new OuvinteIndicadores();
        AtomicBoolean aposCommit = new AtomicBoolean();
        eventos.inscrever(email); eventos.inscrever(inspecao); eventos.inscrever(multa);
        eventos.inscrever(indicadores);
        eventos.inscrever(e -> aposCommit.set(!f.unidadeTrabalho().isAtiva()
                && f.repositorioMovimentacao().buscarEmprestimoAtivo(1,101).isEmpty()));
        emprestar();
        assertTrue(devolucao.registrarDevolucao(1,101,base.plusDays(7)).sucesso());
        assertTrue(aposCommit.get());
        assertEquals(List.of(1), inspecao.getFilaInspecaoLivros());
        assertTrue(multa.getDevolucoesComAtraso().isEmpty());
        assertEquals(1, indicadores.getTotalDevolucoes());
        assertEquals(0, indicadores.getTotalAtrasadas());
        assertTrue(devolucao.emitirBoletoMulta(1,101).isEmpty());
        assertTrue(f.repositorioMovimentacao().listarBoletos().isEmpty());
        assertEquals("DEVOLVIDO", cadastro.consultarLivro(1).orElseThrow().getEstadoNome());
    }
    @Test void R06_submissoesSimultaneasRecebemMesmoResultadoESomamSoTrintaDias() throws Exception {
        emprestar();
        var executor = new ExecutorComando();
        var iniciou = new CountDownLatch(1); var liberar = new CountDownLatch(1);
        AtomicInteger chamadas = new AtomicInteger();
        ComandoCirculacao comando = new ComandoCirculacao() {
            public String idComando() { return "concorrente"; }
            public String autor() { return "teste"; }
            public Instant criadoEm() { return Instant.EPOCH; }
            public ComprovanteOperacao executar() {
                chamadas.incrementAndGet(); iniciou.countDown();
                try { if (!liberar.await(3, TimeUnit.SECONDS)) throw new IllegalStateException("timeout"); }
                catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new IllegalStateException(ex); }
                return facade.renovarEmprestimo(1,101);
            }
        };
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<ComprovanteOperacao> a = pool.submit(() -> executor.executar(comando));
            assertTrue(iniciou.await(2,TimeUnit.SECONDS));
            var segundaIniciou = new CountDownLatch(1);
            Future<ComprovanteOperacao> b = pool.submit(() -> { segundaIniciou.countDown(); return executor.executar(comando); });
            assertTrue(segundaIniciou.await(2,TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> b.get(150,TimeUnit.MILLISECONDS));
            liberar.countDown();
            assertEquals(a.get(2,TimeUnit.SECONDS), b.get(2,TimeUnit.SECONDS));
            assertEquals(1,chamadas.get());
            assertEquals(base.plusDays(37), f.repositorioMovimentacao().buscarEmprestimoAtivo(1,101).orElseThrow().getDataVencimento());
            assertTrue(executor.executar(new RenovarEmprestimoCommand("2","teste",Instant.now(),facade,1,101)).sucesso());
            assertFalse(executor.executar(new RenovarEmprestimoCommand("3","teste",Instant.now(),facade,1,101)).sucesso());
        } finally { liberar.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(3,TimeUnit.SECONDS)); }
    }
    @Test void R09_cacheHitMedidoSemReconsultarMetadadosEFalhaTemFallback() {
        AtomicInteger consultas = new AtomicInteger();
        var baseConsulta = new ConsultaAcervoBase(f.repositorioLivro()) {
            @Override public Optional<MetadadosLivro> consultarMetadados(int id) {
                consultas.incrementAndGet(); return super.consultarMetadados(id);
            }
        };
        var cache = new CacheMetadadosDecorator(baseConsulta);
        var medicao = new MedicaoConsultaDecorator(cache);
        assertEquals(medicao.consultarMetadados(1), medicao.consultarMetadados(1));
        assertEquals(1,consultas.get()); assertEquals(2,medicao.getTotalMedicoes());
        emprestar(); assertFalse(medicao.podeReservar(1));
        cache.setSimularFalhaCache(true);
        assertTrue(medicao.consultarMetadados(1).isPresent()); assertEquals(2,consultas.get());
        assertEquals(4,medicao.getTotalMedicoes());
    }
    @Test void R10_recusaCiclosDuploPaiDuplicacaoETransferenciaInvalida() {
        Set<Integer> ids = new HashSet<>();
        var raiz = new GrupoAcervoComposite("raiz",ids);
        var a = new GrupoAcervoComposite("a",ids); var b = new GrupoAcervoComposite("b",ids);
        var sub = new GrupoAcervoComposite("sub",ids);
        var item = new ItemLivroComposite(1,"GoF"); sub.adicionar(item); a.adicionar(sub);
        raiz.adicionar(a); raiz.adicionar(b);
        assertEquals(List.of(1),raiz.listarIdsLivros());
        assertThrows(IllegalArgumentException.class, () -> sub.adicionar(raiz));
        assertThrows(IllegalArgumentException.class, () -> b.adicionar(sub));
        assertThrows(IllegalArgumentException.class, () -> b.adicionar(new ItemLivroComposite(1,"duplicado")));
        assertThrows(NullPointerException.class, () -> sub.moverItemPara(item,null));
        assertEquals(1,sub.contarLivros());
        emprestar(); var antes = f.repositorioMovimentacao().buscarEmprestimoAtivo(1,101).orElseThrow();
        assertTrue(sub.moverItemPara(item,b)); assertEquals(1,raiz.contarLivros());
        assertEquals("EMPRESTADO",cadastro.consultarLivro(1).orElseThrow().getEstadoNome());
        assertEquals(antes.getDataVencimento(),f.repositorioMovimentacao().buscarEmprestimoAtivo(1,101).orElseThrow().getDataVencimento());
    }
    @Test void R01_livroExcluidoNaoPodeReservar() {
        assertTrue(cadastro.excluirLivro(1));
        assertFalse(facade.solicitarReserva(1,101,base).sucesso());
        assertFalse(new ConsultaAcervoBase(f.repositorioLivro()).podeReservar(1));
    }
    @Test void R02_multaEmitidaPreservaValorEPolitica() {
        emprestar(); assertTrue(devolucao.registrarDevolucao(1,101,base.plusDays(12)).sucesso());
        BoletoMulta boleto = devolucao.emitirBoletoMulta(1,101).orElseThrow();
        config.definirPolitica(new MultaToleranciaDoisDiasStrategy());
        assertEquals(new BigDecimal("5.00"),boleto.valor());
        assertEquals("ORIGINAL_1_REAL_DIA",boleto.politicaAplicada());
        assertEquals(boleto,f.repositorioMovimentacao().listarBoletos().get(0));
        for (PoliticaMulta politica : List.of(new MultaOriginalStrategy(),new MultaTetoVinteStrategy(),new MultaToleranciaDoisDiasStrategy()))
            assertEquals(new BigDecimal("0.00"),politica.calcular(base,base.minusDays(1)));
        assertEquals(new BigDecimal("0.00"),new MultaToleranciaDoisDiasStrategy().calcular(base,base.plusDays(2)));
    }

    @Test void R03_converteDocumentosERejeitaQuitacaoDesconhecidaOuValorDivergente() throws Exception {
        AtomicReference<String> docA = new AtomicReference<>();
        AtomicReference<String> docB = new AtomicReference<>();
        var a = new BancoAAdapter(new BancoASimulado() {
            @Override public String criarBoletoCentavos(String doc,long valor) { docA.set(doc); return super.criarBoletoCentavos(doc,valor); }
        });
        var b = new BancoBAdapter(new BancoBSimulado() {
            @Override public String gerarCobrancaDecimal(String doc,double valor) { docB.set(doc); return super.gerarCobrancaDecimal(doc,valor); }
        });
        String idA=a.registrarCobranca("ref","111.222.333-44",new BigDecimal("7.00"));
        String idB=b.registrarCobranca("ref","11122233344",new BigDecimal("7.00"));
        assertEquals("11122233344",docA.get()); assertEquals("111.222.333-44",docB.get());
        assertTrue(a.processarNotificacao(new BancoASimulado.NotificacaoBancoA("desconhecida",700,"LIQUIDADO")).isEmpty());
        assertTrue(a.processarNotificacao(new BancoASimulado.NotificacaoBancoA(idA,600,"LIQUIDADO")).isEmpty());
        assertFalse(a.isQuitada(idA));
        assertTrue(b.processarNotificacao(new BancoBSimulado.PayloadBancoB(idB,Double.NaN,"PAGO")).isEmpty());
        assertTrue(b.processarNotificacao(new BancoBSimulado.PayloadBancoB(idB,6,"PAGO")).isEmpty());
        assertFalse(b.isQuitada(idB));
        assertTrue(b.processarNotificacao("payload inválido").isEmpty());
        assertEquals("PENDENTE",b.processarNotificacao(new BancoBSimulado.PayloadBancoB(idB,0,"ABERTO")).orElseThrow().situacaoPadronizada());
        var pool=Executors.newFixedThreadPool(2); var iniciar=new CountDownLatch(1);
        try {
            Callable<Boolean> pagar=()-> { iniciar.await(); return b.processarNotificacao(new BancoBSimulado.PayloadBancoB(idB,7,"PAGO")).isPresent(); };
            var x=pool.submit(pagar); var y=pool.submit(pagar); iniciar.countDown();
            assertNotEquals(x.get(2,TimeUnit.SECONDS),y.get(2,TimeUnit.SECONDS));
            assertTrue(b.isQuitada(idB));
        } finally { pool.shutdownNow(); assertTrue(pool.awaitTermination(3,TimeUnit.SECONDS)); }
    }
    @Test void R04_falhaAoIniciarRetornaComprovanteSemPublicar() {
        AtomicInteger notificacoes=new AtomicInteger(); eventos.inscrever(e -> notificacoes.incrementAndGet());
        UnidadeTrabalho falha=new UnidadeTrabalho() {
            public void iniciar() { throw new IllegalStateException("conexão indisponível"); }
            public void commit() { fail("não deveria confirmar"); }
            public void rollback() { fail("não deveria desfazer uma transação inexistente"); }
            public boolean isAtiva() { return false; }
        };
        // Repositórios não são acessados se o início falhar.
        RepositorioLivro livros=(RepositorioLivro)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{RepositorioLivro.class},(proxy,method,args)-> { if(method.getName().equals("unidadeTrabalho")) return null; throw new AssertionError(); });
        RepositorioMovimentacao mov=(RepositorioMovimentacao)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{RepositorioMovimentacao.class},(proxy,method,args)-> { if(method.getName().equals("unidadeTrabalho")) return null; throw new AssertionError(); });
        RepositorioRelacionamento rel=(RepositorioRelacionamento)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{RepositorioRelacionamento.class},(proxy,method,args)-> { if(method.getName().equals("unidadeTrabalho")) return null; throw new AssertionError(); });
        var fachada=new CirculacaoFacade(falha,livros,mov,rel,eventos);
        assertFalse(fachada.registrarDevolucao(1,101,base).sucesso());
        assertEquals(0,notificacoes.get());
    }
    @Test void R01_R04_datasInvalidasEEventosDeOutroLivroNaoAlteramDominio() {
        assertTrue(facade.solicitarReserva(1,101,base).sucesso());
        assertFalse(facade.efetivarEmprestimo(1,101,base,-1).sucesso());
        assertTrue(facade.existeReservaAtiva(1,101));
        assertTrue(facade.efetivarEmprestimo(1,101,base,7).sucesso());
        assertFalse(facade.registrarDevolucao(1,101,base.minusDays(1)).sucesso());
        Livro livro=f.repositorioLivro().buscarPorId(1).orElseThrow();
        var outro=new Emprestimo(2,101,base,base.plusDays(7));
        assertFalse(livro.renovar(outro)); assertFalse(livro.devolver(outro));
        assertFalse(livro.reservar(101)); assertFalse(livro.inspecionar(true));
        assertEquals("EMPRESTADO",livro.getEstadoNome()); assertEquals(0,livro.getNumRenovacoes());
        assertEquals(base.plusDays(7),outro.getDataVencimento());
    }
    @Test void R08_recusaMisturaDeFabricasEAlteracoesSemSalvarNaoVazam() {
        var outra=new MemoriaPersistenciaFactory();
        assertThrows(IllegalArgumentException.class,()->new CirculacaoFacade(f.unidadeTrabalho(),
                outra.repositorioLivro(),f.repositorioMovimentacao(),f.repositorioRelacionamento(),eventos));
        Livro l=f.repositorioLivro().buscarPorId(1).orElseThrow(); l.reservar(101);
        assertEquals("LIVRE",f.repositorioLivro().buscarPorId(1).orElseThrow().getEstadoNome());
        f.unidadeTrabalho().iniciar();
        f.repositorioRelacionamento().salvarPessoa(new Pessoa("Outra","Pessoa","999"));
        f.unidadeTrabalho().rollback();
        assertTrue(f.repositorioRelacionamento().buscarPessoaPorCpf("999").isEmpty());
    }
    @Test void R07_ordemDoTemplateFinalEValidacaoSemGravacao() throws Exception {
        var ordem=new ArrayList<String>();
        var template=new br.pucpr.bcc.biblioteca.r07_template.RotinaCadastroTemplate<String>() {
            protected Optional<String> buscarExistente(){ ordem.add("buscar"); return Optional.empty(); }
            protected void validar(){ ordem.add("validar"); }
            protected void verificarPreRequisitos(){ ordem.add("pre"); }
            protected String criar(){ ordem.add("criar"); return "cadastro"; }
            protected void armazenar(String e){ ordem.add("armazenar"); }
            protected void apresentar(String e){ ordem.add("apresentar"); }
        };
        assertTrue(template.incluir().sucesso());
        assertEquals(List.of("buscar","validar","pre","criar","armazenar","apresentar"),ordem);
        assertTrue(java.lang.reflect.Modifier.isFinal(template.getClass().getMethod("incluir").getModifiers()));
        assertThrows(IllegalArgumentException.class,()->cadastro.inserirPessoa("Ana","Silva",null));
        assertThrows(IllegalArgumentException.class,()->cadastro.inserirPessoa("Ana",null,"999"));
        assertTrue(cadastro.consultarPessoa("999").isEmpty());
    }
    @Test void R05_rollbackDaDevolucaoNaoPublicaEvento() {
        emprestar(); AtomicInteger eventosRecebidos=new AtomicInteger(); eventos.inscrever(e -> eventosRecebidos.incrementAndGet());
        RepositorioLivro falho=new RepositorioLivro() {
            public Optional<Livro> buscarPorId(int id) { return f.repositorioLivro().buscarPorId(id); }
            public void salvar(Livro l) { throw new IllegalStateException("falha ao gravar estado"); }
            public List<Livro> listarTodos(){return f.repositorioLivro().listarTodos();}
            public void remover(int id){throw new UnsupportedOperationException();}
        };
        var fachada=new CirculacaoFacade(f.unidadeTrabalho(),falho,f.repositorioMovimentacao(),f.repositorioRelacionamento(),eventos);
        assertFalse(fachada.registrarDevolucao(1,101,base.plusDays(10)).sucesso());
        assertEquals(0,eventosRecebidos.get());
        assertTrue(f.repositorioMovimentacao().buscarDevolucao(1,101).isEmpty());
        assertTrue(f.repositorioMovimentacao().buscarEmprestimoAtivo(1,101).isPresent());
        assertEquals("EMPRESTADO",cadastro.consultarLivro(1).orElseThrow().getEstadoNome());
    }
    @Test void R08_memoriaImpedeOrfaosComoFamiliaRelacional() {
        assertThrows(IllegalStateException.class,()->f.repositorioMovimentacao().salvarReserva(new Reserva(99,101,base)));
        emprestar();
        assertThrows(IllegalStateException.class,()->cadastro.excluirEstudante(101));
        assertThrows(IllegalStateException.class,()->cadastro.excluirPessoa("11122233344"));
        assertThrows(IllegalStateException.class,()->f.repositorioLivro().remover(1));
        assertTrue(f.repositorioMovimentacao().buscarEmprestimoAtivo(1,101).isPresent());
    }
    @Test void R02_demoCompletaEmiteCincoReaisAposRenovacao() {
        var original=System.out; var bytes=new java.io.ByteArrayOutputStream();
        try (var saida=new java.io.PrintStream(bytes, true, java.nio.charset.StandardCharsets.UTF_8)) {
            System.setOut(saida);
            br.pucpr.bcc.biblioteca.ui.ConsoleApp.main(new String[]{"demo"});
        } finally { System.setOut(original); }
        String texto=bytes.toString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(texto.contains("valor=5.00")); assertFalse(texto.contains("sucesso=false"));
        assertTrue(texto.contains("dataVencimento=2026-12-27"));
    }
    @Test void R03_novasInstanciasDoBancoNaoReutilizamIdentificadoresPersistidos() {
        String a1=new BancoASimulado().criarBoletoCentavos("11122233344",700);
        String a2=new BancoASimulado().criarBoletoCentavos("11122233344",700);
        String b1=new BancoBSimulado().gerarCobrancaDecimal("111.222.333-44",7);
        String b2=new BancoBSimulado().gerarCobrancaDecimal("111.222.333-44",7);
        assertNotEquals(a1,a2); assertNotEquals(b1,b2); assertNotEquals(a1,b1);
        assertTrue(a1.length()<=60); assertTrue(b1.length()<=60);
    }
}
