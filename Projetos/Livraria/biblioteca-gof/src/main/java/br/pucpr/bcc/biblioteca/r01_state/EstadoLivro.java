package br.pucpr.bcc.biblioteca.r01_state;

import br.pucpr.bcc.biblioteca.dominio.Emprestimo;
import br.pucpr.bcc.biblioteca.dominio.Livro;

public interface EstadoLivro {
    String nome();
    boolean reservar(Livro livro, int idEstudante);
    boolean emprestar(Livro livro, int idEstudante, Emprestimo emprestimo);
    boolean renovar(Livro livro, Emprestimo emprestimo);
    boolean devolver(Livro livro, Emprestimo emprestimo);
    boolean inspecionar(Livro livro, boolean bomEstado);
    boolean enviarParaReparo(Livro livro, boolean danoIdentificado);
    boolean retomarDoReparo(Livro livro);
    boolean incinerar(Livro livro, boolean semReparo);
    boolean excluirLivro(Livro livro);
}
