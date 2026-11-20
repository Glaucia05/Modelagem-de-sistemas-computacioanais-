package br.pucpr.bcc.biblioteca.interfaces;

import br.pucpr.bcc.biblioteca.dominio.Livro;
import java.util.Optional;

public interface iLivro {
    Livro inserirLivro(int idLivro, String titulo);
    Livro atualizarLivro(int idLivro, String novoTitulo);
    Optional<Livro> consultarLivro(int idLivro);
    boolean excluirLivro(int idLivro);
}
