package br.pucpr.bcc.biblioteca.tratadores;

import br.pucpr.bcc.biblioteca.dominio.BoletoMulta;
import br.pucpr.bcc.biblioteca.dominio.Devolucao;
import br.pucpr.bcc.biblioteca.interfaces.iDevolucao;
import br.pucpr.bcc.biblioteca.financeiro.multa.ConfiguracaoMulta;
import br.pucpr.bcc.biblioteca.financeiro.cobranca.CobrancaInstitucional;
import br.pucpr.bcc.biblioteca.circulacao.CirculacaoFacade;
import br.pucpr.bcc.biblioteca.circulacao.ComprovanteOperacao;
import br.pucpr.bcc.biblioteca.persistencia.Movimentacao;
import br.pucpr.bcc.biblioteca.persistencia.GruposDoSistema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class TratadorDeDevolucao implements iDevolucao {
    private final CirculacaoFacade facade;
    private final Movimentacao movimentacao;
    private final GruposDoSistema grupos;
    private final ConfiguracaoMulta configuracaoMulta;
    private final CobrancaInstitucional cobranca;

    public TratadorDeDevolucao(
            CirculacaoFacade facade,
            Movimentacao movimentacao,
            GruposDoSistema grupos,
            ConfiguracaoMulta configuracaoMulta,
            CobrancaInstitucional cobranca
    ) {
        this.facade = facade;
        this.movimentacao = movimentacao;
        this.grupos = grupos;
        this.configuracaoMulta = configuracaoMulta;
        this.cobranca = cobranca;
    }

    public void definirPoliticaMulta(String nome) {
        configuracaoMulta.definirPolitica(switch (nome.toLowerCase(java.util.Locale.ROOT)) {
            case "original" -> new br.pucpr.bcc.biblioteca.financeiro.multa.MultaOriginalStrategy();
            case "teto" -> new br.pucpr.bcc.biblioteca.financeiro.multa.MultaTetoVinteStrategy();
            case "tolerancia" -> new br.pucpr.bcc.biblioteca.financeiro.multa.MultaToleranciaDoisDiasStrategy();
            default -> throw new IllegalArgumentException("Política deve ser original, teto ou tolerancia");
        });
    }

    @Override
    public ComprovanteOperacao registrarDevolucao(int idLivro, int idEstudante, LocalDate dataDevolucao) {
        return facade.registrarDevolucao(idLivro, idEstudante, dataDevolucao);
    }

    @Override
    public Optional<BoletoMulta> emitirBoletoMulta(int idLivro, int idEstudante) {
        Optional<Devolucao> optDev = movimentacao.consultarDevolucao(idLivro, idEstudante);
        if (optDev.isEmpty()) return Optional.empty();

        Devolucao dev = optDev.get();
        var politica = configuracaoMulta.getPoliticaVigente();
        BigDecimal valor = politica.calcular(dev.getDataVencimento(), dev.getDataDevolucao());
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        String cpf = grupos.consultarEstudante(idEstudante).map(e -> e.cpf()).orElseThrow(() -> new IllegalStateException("Estudante não encontrado para emissão"));
        String idCobranca = cobranca.registrarCobranca("DEV-" + idLivro + "-" + idEstudante, cpf, valor);
        BoletoMulta boleto = new BoletoMulta(
                idCobranca,
                idLivro,
                idEstudante,
                valor,
                politica.nomePolitica(),
                "EMITIDO"
        );
        movimentacao.salvarBoleto(boleto);
        return Optional.of(boleto);
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
