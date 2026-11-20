package br.pucpr.bcc.biblioteca.r08_abstract_factory;

import br.pucpr.bcc.biblioteca.dominio.Livro;
import java.util.List;
import java.util.Optional;

public interface RepositorioLivro {
    void salvar(Livro livro);
    Optional<Livro> buscarPorId(int idLivro);
    List<Livro> listarTodos();
    void remover(int idLivro);
}
