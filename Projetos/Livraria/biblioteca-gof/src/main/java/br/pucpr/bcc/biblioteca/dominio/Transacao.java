package br.pucpr.bcc.biblioteca.dominio;

import java.time.LocalDate;

/**
 * Superclasse das transações de circulação (Diagrama de Domínio e Diagrama de Classes de Projeto):
 * Reserva, Emprestimo e Devolucao. Uma transação envolve um Livro e é realizada por um Estudante
 * em uma data.
 */
public abstract class Transacao {
    private final int idLivro;
    private final int idEstudante;
    private final LocalDate data;

    protected Transacao(int idLivro, int idEstudante, LocalDate data) {
        this.idLivro = idLivro;
        this.idEstudante = idEstudante;
        this.data = data;
    }

    public int getIdLivro() { return idLivro; }
    public int getIdEstudante() { return idEstudante; }
    public LocalDate getData() { return data; }
}
