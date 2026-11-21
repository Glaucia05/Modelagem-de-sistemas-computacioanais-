package br.pucpr.bcc.biblioteca.persistencia;

import br.pucpr.bcc.biblioteca.dominio.Livro;
import java.util.List;
import java.util.Optional;

public interface AcervoDeLivros {
    default UnidadeTrabalho unidadeTrabalho() { return null; }
    void inserirLivro(Livro livro);
    Optional<Livro> consultarLivro(int idLivro);
    List<Livro> listarLivros();
    void excluirLivro(int idLivro);
}
