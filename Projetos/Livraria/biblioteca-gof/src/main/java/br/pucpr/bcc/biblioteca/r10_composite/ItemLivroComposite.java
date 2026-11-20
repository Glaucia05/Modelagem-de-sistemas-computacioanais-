package br.pucpr.bcc.biblioteca.r10_composite;

import java.util.List;

public record ItemLivroComposite(int idLivro, String tituloReferencia) implements ElementoAcervo {
    @Override public String nome() { return tituloReferencia; }
    @Override public int contarLivros() { return 1; }
    @Override public List<Integer> listarIdsLivros() { return List.of(idLivro); }
    @Override public boolean contemLivro(int idLivro) { return this.idLivro == idLivro; }
}
