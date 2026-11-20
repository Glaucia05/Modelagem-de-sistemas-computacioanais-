package br.pucpr.bcc.biblioteca.r07_template;

import br.pucpr.bcc.biblioteca.dominio.Livro;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.RepositorioLivro;
import java.util.Optional;

public class CadastroLivroTemplate extends RotinaCadastroTemplate<Livro> {
    private final RepositorioLivro repoLivro;
    private final int idLivro;
    private final String titulo;

    public CadastroLivroTemplate(RepositorioLivro repoLivro, int idLivro, String titulo) {
        this.repoLivro = repoLivro;
        this.idLivro = idLivro;
        this.titulo = titulo;
    }

    @Override protected Optional<Livro> buscarExistente() { return repoLivro.buscarPorId(idLivro); }

    @Override
    protected void validar() {
        if (idLivro <= 0 || titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Dados inválidos de Livro");
        }
    }

    @Override protected void verificarPreRequisitos() {}
    @Override protected Livro criar() { return new Livro(idLivro, titulo); }
    @Override protected void armazenar(Livro entidade) { repoLivro.salvar(entidade); }
}
