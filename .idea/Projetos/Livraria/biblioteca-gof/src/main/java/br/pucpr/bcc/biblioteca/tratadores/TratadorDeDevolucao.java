package br.pucpr.bcc.biblioteca.tratadores;

import br.pucpr.bcc.biblioteca.dominio.BoletoMulta;
import br.pucpr.bcc.biblioteca.dominio.Devolucao;
import br.pucpr.bcc.biblioteca.interfaces.iDevolucao;
import br.pucpr.bcc.biblioteca.r02_strategy.ConfiguracaoMulta;
import br.pucpr.bcc.biblioteca.r03_adapter.CobrancaInstitucional;
import br.pucpr.bcc.biblioteca.r04_facade.CirculacaoFacade;
import br.pucpr.bcc.biblioteca.r04_facade.ComprovanteOperacao;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.RepositorioMovimentacao;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.RepositorioRelacionamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class TratadorDeDevolucao implements iDevolucao {
    private final CirculacaoFacade facade;
    private final RepositorioMovimentacao repoMov;
    private final RepositorioRelacionamento repoRel;
    private final ConfiguracaoMulta configuracaoMulta;
    private final CobrancaInstitucional cobranca;

    public TratadorDeDevolucao(
            CirculacaoFacade facade,
            RepositorioMovimentacao repoMov,
            RepositorioRelacionamento repoRel,
            ConfiguracaoMulta configuracaoMulta,
            CobrancaInstitucional cobranca
    ) {
        this.facade = facade;
        this.repoMov = repoMov;
        this.repoRel = repoRel;
        this.configuracaoMulta = configuracaoMulta;
        this.cobranca = cobranca;
    }

    @Override
    public ComprovanteOperacao registrarDevolucao(int idLivro, int idEstudante, LocalDate dataDevolucao) {
        return facade.registrarDevolucao(idLivro, idEstudante, dataDevolucao);
    }

    @Override
    public Optional<BoletoMulta> emitirBoletoMulta(int idLivro, int idEstudante) {
        Optional<Devolucao> optDev = repoMov.buscarDevolucao(idLivro, idEstudante);
        if (optDev.isEmpty()) return Optional.empty();

        Devolucao dev = optDev.get();
        BigDecimal valor = configuracaoMulta.getPoliticaVigente().calcular(dev.getDataVencimento(), dev.getDataDevolucao());
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            return Optional.empty();
        }

        String cpf = repoRel.buscarEstudantePorId(idEstudante).map(e -> e.cpf()).orElse("00000000000");
        String idCobranca = cobranca.registrarCobranca("DEV-" + idLivro + "-" + idEstudante, cpf, valor);
        BoletoMulta boleto = new BoletoMulta(
                idCobranca,
                idLivro,
                idEstudante,
                valor,
                configuracaoMulta.getPoliticaVigente().nomePolitica(),
                "EMITIDO"
        );
        repoMov.salvarBoleto(boleto);
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
