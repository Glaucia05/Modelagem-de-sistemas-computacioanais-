package br.pucpr.bcc.biblioteca.dominio;

import java.time.LocalDate;

public class Emprestimo {
    private final int idLivro;
    private final int idEstudante;
    private final LocalDate dataInicio;
    private LocalDate dataVencimento;
    private String estado = "EMPRESTADO";
    private boolean devolvido = false;

    public Emprestimo(int idLivro, int idEstudante, LocalDate dataInicio, LocalDate dataVencimento) {
        this.idLivro = idLivro;
        this.idEstudante = idEstudante;
        this.dataInicio = dataInicio;
        this.dataVencimento = dataVencimento;
    }

    public int getIdLivro() { return idLivro; }
    public int getIdEstudante() { return idEstudante; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public String getEstado() { return estado; }
    public boolean isDevolvido() { return devolvido; }

    public void estenderVencimento(int dias) {
        this.dataVencimento = this.dataVencimento.plusDays(dias);
        this.estado = "RENOVADO";
    }

    public void marcarDevolvido() {
        this.devolvido = true;
        this.estado = "DEVOLVIDO";
    }
}
