package br.pucpr.bcc.biblioteca.r07_template;

import br.pucpr.bcc.biblioteca.dominio.Livro;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.AcervoDeLivros;
import java.util.Optional;

public class CadastroLivroTemplate extends RotinaCadastroTemplate<Livro> {
    private final AcervoDeLivros acervo;
    private final int idLivro;
    private final String titulo;

    public CadastroLivroTemplate(AcervoDeLivros acervo, int idLivro, String titulo) {
        this.acervo = acervo;
        this.idLivro = idLivro;
        this.titulo = titulo;
    }

    @Override protected Optional<Livro> buscarExistente() { return acervo.consultarLivro(idLivro); }

    @Override
    protected void validar() {
        if (idLivro <= 0 || titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Dados inválidos de Livro");
        }
    }

    @Override protected void verificarPreRequisitos() {}
    @Override protected Livro criar() { return new Livro(idLivro, titulo); }
    @Override protected void armazenar(Livro entidade) { acervo.salvar(entidade); }
}
