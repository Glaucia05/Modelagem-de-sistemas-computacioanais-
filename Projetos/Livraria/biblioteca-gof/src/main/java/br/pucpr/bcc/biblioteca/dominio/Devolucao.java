package br.pucpr.bcc.biblioteca.dominio;

import java.time.LocalDate;

/** Transação de devolução: a data da transação é a data de devolução. */
public class Devolucao extends Transacao {
    private final long idDevolucao;
    private final LocalDate dataVencimento;

    public Devolucao(int idLivro, int idEstudante, LocalDate dataDevolucao, LocalDate dataVencimento) {
        this(0, idLivro, idEstudante, dataDevolucao, dataVencimento);
    }

    public Devolucao(long idDevolucao, int idLivro, int idEstudante, LocalDate dataDevolucao, LocalDate dataVencimento) {
        super(idLivro, idEstudante, dataDevolucao);
        this.idDevolucao = idDevolucao;
        this.dataVencimento = dataVencimento;
    }

    public long getIdDevolucao() { return idDevolucao; }
    public LocalDate getDataDevolucao() { return getData(); }
    public LocalDate getDataVencimento() { return dataVencimento; }
}
