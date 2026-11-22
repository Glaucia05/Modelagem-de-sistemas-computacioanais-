package br.pucpr.bcc.biblioteca.r10_composite;

import java.util.List;

public interface ElementoAcervo {
    String nome();
    int contarLivros();
    List<Integer> listarIdsLivros();
    boolean contemLivro(int idLivro);
}
