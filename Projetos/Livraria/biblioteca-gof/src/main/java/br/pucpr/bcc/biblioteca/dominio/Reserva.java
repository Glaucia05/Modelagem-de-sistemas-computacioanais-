package br.pucpr.bcc.biblioteca.dominio;

import java.time.LocalDate;

public class Reserva extends Transacao {
    private boolean ativa = true;

    public Reserva(int idLivro, int idEstudante, LocalDate data) {
        super(idLivro, idEstudante, data);
    }

    public boolean isAtiva() { return ativa; }
    public void cancelar() { this.ativa = false; }
}
