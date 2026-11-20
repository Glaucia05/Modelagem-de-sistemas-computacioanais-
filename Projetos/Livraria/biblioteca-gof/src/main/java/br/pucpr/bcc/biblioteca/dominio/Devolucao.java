package br.pucpr.bcc.biblioteca.dominio;

import java.time.LocalDate;

public class Devolucao {
    private final int idLivro;
    private final int idEstudante;
    private final LocalDate dataDevolucao;
    private final LocalDate dataVencimento;

    public Devolucao(int idLivro, int idEstudante, LocalDate dataDevolucao, LocalDate dataVencimento) {
        this.idLivro = idLivro;
        this.idEstudante = idEstudante;
        this.dataDevolucao = dataDevolucao;
        this.dataVencimento = dataVencimento;
    }

    public int getIdLivro() { return idLivro; }
    public int getIdEstudante() { return idEstudante; }
    public LocalDate getDataDevolucao() { return dataDevolucao; }
    public LocalDate getDataVencimento() { return dataVencimento; }
}
