package br.pucpr.bcc.biblioteca.r06_command;

import br.pucpr.bcc.biblioteca.r04_facade.CirculacaoFacade;
import br.pucpr.bcc.biblioteca.r04_facade.ComprovanteOperacao;
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
