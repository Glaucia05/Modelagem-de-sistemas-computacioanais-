package br.pucpr.bcc.biblioteca.estado;

import br.pucpr.bcc.biblioteca.dominio.Emprestimo;
import br.pucpr.bcc.biblioteca.dominio.Livro;

public abstract class AbstractEstadoLivro implements EstadoLivro {
    @Override public boolean reservar(Livro livro, int idEstudante) { return false; }
    @Override public boolean emprestar(Livro livro, int idEstudante, Emprestimo emprestimo) { return false; }
    @Override public boolean renovar(Livro livro, Emprestimo emprestimo) { return false; }
    @Override public boolean devolver(Livro livro, Emprestimo emprestimo) { return false; }
    @Override public boolean inspecionar(Livro livro, boolean bomEstado) { return false; }
    @Override public boolean enviarParaReparo(Livro livro, boolean danoIdentificado) { return false; }
    @Override public boolean retomarDoReparo(Livro livro) { return false; }
    @Override public boolean incinerar(Livro livro, boolean semReparo) { return false; }
    @Override public boolean excluirLivro(Livro livro) { return false; }
}
