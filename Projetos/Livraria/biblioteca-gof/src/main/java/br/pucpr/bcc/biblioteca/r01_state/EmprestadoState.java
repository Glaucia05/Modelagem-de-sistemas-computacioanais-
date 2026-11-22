package br.pucpr.bcc.biblioteca.r01_state;

import br.pucpr.bcc.biblioteca.dominio.Emprestimo;
import br.pucpr.bcc.biblioteca.dominio.Livro;

public class EmprestadoState extends AbstractEstadoLivro {
    @Override public String nome() { return "EMPRESTADO"; }

    @Override
    public boolean renovar(Livro livro, Emprestimo emprestimo) {
        if (emprestimo == null || emprestimo.isDevolvido() || emprestimo.getIdLivro() != livro.getIdLivro() || livro.getNumRenovacoes() >= 2) {
            return false;
        }
        emprestimo.renovar(30);
        livro.incrementarRenovacoes();
        livro.definirEstadoInterno(new RenovadoState());
        return true;
    }

    @Override
    public boolean devolver(Livro livro, Emprestimo emprestimo) {
        if (emprestimo == null || emprestimo.isDevolvido() || emprestimo.getIdLivro() != livro.getIdLivro()) return false;
        livro.definirEstadoInterno(new DevolvidoState());
        return true;
    }
}
