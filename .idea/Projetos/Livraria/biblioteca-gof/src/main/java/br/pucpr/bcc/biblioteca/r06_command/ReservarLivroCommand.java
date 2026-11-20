package br.pucpr.bcc.biblioteca.r06_command;

import br.pucpr.bcc.biblioteca.r04_facade.CirculacaoFacade;
import br.pucpr.bcc.biblioteca.r04_facade.ComprovanteOperacao;
import java.time.Instant;
import java.time.LocalDate;

public record ReservarLivroCommand(
        String idComando,
        String autor,
        Instant criadoEm,
        CirculacaoFacade facade,
        int idLivro,
        int idEstudante,
        LocalDate data
) implements ComandoCirculacao {
    @Override
    public ComprovanteOperacao executar() {
        return facade.solicitarReserva(idLivro, idEstudante, data);
    }
}
