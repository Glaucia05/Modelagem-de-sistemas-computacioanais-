package br.pucpr.bcc.biblioteca.comandos;

import br.pucpr.bcc.biblioteca.circulacao.CirculacaoFacade;
import br.pucpr.bcc.biblioteca.circulacao.ComprovanteOperacao;
import java.time.Instant;

public record RenovarEmprestimoCommand(
        String idComando,
        String autor,
        Instant criadoEm,
        CirculacaoFacade facade,
        int idLivro,
        int idEstudante
) implements ComandoCirculacao {
    @Override
    public ComprovanteOperacao executar() {
        return facade.renovarEmprestimo(idLivro, idEstudante);
    }
}
