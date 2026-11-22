package br.pucpr.bcc.biblioteca.r08_abstract_factory;

import br.pucpr.bcc.biblioteca.dominio.Livro;
import java.util.List;
import java.util.Optional;

public interface AcervoDeLivros {
    default UnidadeTrabalho unidadeTrabalho() { return null; }
    void salvar(Livro livro);
    Optional<Livro> consultarLivro(int idLivro);
    List<Livro> listarTodos();
    void excluirLivro(int idLivro);
}
