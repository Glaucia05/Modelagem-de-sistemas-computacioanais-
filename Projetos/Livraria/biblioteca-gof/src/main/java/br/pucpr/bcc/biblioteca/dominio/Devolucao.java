package br.pucpr.bcc.biblioteca.dominio;

import java.time.LocalDate;

/** Transação de devolução: a data da transação é a data de devolução. */
public class Devolucao extends Transacao {
    private final LocalDate dataVencimento;

    public Devolucao(int idLivro, int idEstudante, LocalDate dataDevolucao, LocalDate dataVencimento) {
        super(idLivro, idEstudante, dataDevolucao);
        this.dataVencimento = dataVencimento;
    }

    public LocalDate getDataDevolucao() { return getData(); }
    public LocalDate getDataVencimento() { return dataVencimento; }
}
