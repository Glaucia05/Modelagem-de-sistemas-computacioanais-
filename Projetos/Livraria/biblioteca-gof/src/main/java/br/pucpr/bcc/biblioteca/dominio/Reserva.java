package br.pucpr.bcc.biblioteca.dominio;

import java.time.LocalDate;

public class Reserva {
    private final int idLivro;
    private final int idEstudante;
    private final LocalDate data;
    private boolean ativa = true;

    public Reserva(int idLivro, int idEstudante, LocalDate data) {
        this.idLivro = idLivro;
        this.idEstudante = idEstudante;
        this.data = data;
    }

    public int getIdLivro() { return idLivro; }
    public int getIdEstudante() { return idEstudante; }
    public LocalDate getData() { return data; }
    public boolean isAtiva() { return ativa; }
    public void cancelar() { this.ativa = false; }
}
