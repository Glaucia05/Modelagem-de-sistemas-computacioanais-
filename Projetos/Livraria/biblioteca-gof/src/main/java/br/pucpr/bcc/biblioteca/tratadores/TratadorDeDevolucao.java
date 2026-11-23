package br.pucpr.bcc.biblioteca.tratadores;

import br.pucpr.bcc.biblioteca.dominio.BoletoMulta;
import br.pucpr.bcc.biblioteca.dominio.Devolucao;
import br.pucpr.bcc.biblioteca.interfaces.iDevolucao;
import br.pucpr.bcc.biblioteca.r02_strategy.ConfiguracaoMulta;
import br.pucpr.bcc.biblioteca.r03_adapter.CobrancaInstitucional;
import br.pucpr.bcc.biblioteca.r04_facade.CirculacaoFacade;
import br.pucpr.bcc.biblioteca.r04_facade.ComprovanteOperacao;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.Movimentacao;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.Relacionamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class TratadorDeDevolucao implements iDevolucao {
    private final CirculacaoFacade facade;
    private final Movimentacao movimentacao;
    private final Relacionamento relacionamento;
    private final ConfiguracaoMulta configuracaoMulta;
    private final CobrancaInstitucional cobranca;

    public TratadorDeDevolucao(
            CirculacaoFacade facade,
            Movimentacao movimentacao,
            Relacionamento relacionamento,
            ConfiguracaoMulta configuracaoMulta,
            CobrancaInstitucional cobranca
    ) {
        this.facade = facade;
        this.movimentacao = movimentacao;
        this.relacionamento = relacionamento;
        this.configuracaoMulta = configuracaoMulta;
        this.cobranca = cobranca;
    }

    public void definirPoliticaMulta(String nome) {
        configuracaoMulta.definirPolitica(switch (nome.toLowerCase(java.util.Locale.ROOT)) {
            case "original" -> new br.pucpr.bcc.biblioteca.r02_strategy.MultaOriginalStrategy();
            case "teto" -> new br.pucpr.bcc.biblioteca.r02_strategy.MultaTetoVinteStrategy();
            case "tolerancia" -> new br.pucpr.bcc.biblioteca.r02_strategy.MultaToleranciaDoisDiasStrategy();
            default -> throw new IllegalArgumentException("Política deve ser original, teto ou tolerancia");
        });
    }

    /**
     * Operação {@code calcularMulta(dataVencimento, dataDevolucao)} do Diagrama de Classes de Projeto.
     * Usa a política vigente (Strategy, R02); o valor monetário é BigDecimal, não double.
     */
    public BigDecimal calcularMulta(LocalDate dataVencimento, LocalDate dataDevolucao) {
        return configuracaoMulta.getPoliticaVigente().calcular(dataVencimento, dataDevolucao);
    }

    @Override
    public ComprovanteOperacao registrarDevolucao(int idLivro, int idEstudante, LocalDate dataDevolucao) {
        return facade.registrarDevolucao(idLivro, idEstudante, dataDevolucao);
    }

    @Override
    public Optional<BoletoMulta> emitirBoletoMulta(int idLivro, int idEstudante) {
        synchronized (movimentacao.unidadeTrabalho() == null ? movimentacao : movimentacao.unidadeTrabalho()) {
            return emitirMulta(idLivro, idEstudante);
        }
    }

    private Optional<BoletoMulta> emitirMulta(int idLivro, int idEstudante) {
        Optional<Devolucao> optDev = movimentacao.consultarDevolucao(idLivro, idEstudante);
        if (optDev.isEmpty()) return Optional.empty();

        Devolucao dev = optDev.get();
        Optional<BoletoMulta> existente = movimentacao.listarBoletos().stream()
                .filter(b -> b.idDevolucao() == dev.getIdDevolucao()).findFirst();
        if (existente.isPresent()) return existente;
        var politica = configuracaoMulta.getPoliticaVigente();
        BigDecimal valor = politica.calcular(dev.getDataVencimento(), dev.getDataDevolucao());
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        String cpf = relacionamento.consultarEstudante(idEstudante).map(e -> e.cpf()).orElseThrow(() -> new IllegalStateException("Estudante não encontrado para emissão"));
        String idCobranca = cobranca.registrarCobranca("DEV-" + dev.getIdDevolucao(), cpf, valor);
        BoletoMulta boleto = new BoletoMulta(
                idCobranca,
                dev.getIdDevolucao(),
                idLivro,
                idEstudante,
                valor,
                politica.nomePolitica(),
                "EMITIDO"
        );
        movimentacao.salvarBoleto(boleto);
        return Optional.of(boleto);
    }

    public boolean registrarPagamento(Object notificacaoBruta) {
        synchronized (movimentacao.unidadeTrabalho() == null ? movimentacao : movimentacao.unidadeTrabalho()) {
            var notificacao = cobranca.processarNotificacao(notificacaoBruta);
            if (notificacao.isEmpty() || !"QUITADO".equals(notificacao.get().situacaoPadronizada())) return false;
            var pagamento = notificacao.get();
            for (BoletoMulta boleto : movimentacao.listarBoletos()) {
                if (boleto.idCobranca().equals(pagamento.idCobranca()) && !"QUITADO".equals(boleto.situacao())
                        && boleto.valor().compareTo(pagamento.valorPago()) == 0) {
                    movimentacao.salvarBoleto(new BoletoMulta(boleto.idCobranca(), boleto.idDevolucao(), boleto.idLivro(),
                            boleto.idEstudante(), boleto.valor(), boleto.politicaAplicada(), "QUITADO"));
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public ComprovanteOperacao inspecionarLivro(int idLivro, boolean bomEstado) {
        return facade.inspecionarLivro(idLivro, bomEstado);
    }

    @Override
    public ComprovanteOperacao emitirLaudoIrrecuperavel(int idLivro) {
        return facade.emitirLaudoIrrecuperavel(idLivro);
    }
}
