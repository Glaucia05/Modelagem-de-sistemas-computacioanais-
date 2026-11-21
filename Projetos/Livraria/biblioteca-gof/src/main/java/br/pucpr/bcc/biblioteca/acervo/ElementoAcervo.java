package br.pucpr.bcc.biblioteca.acervo;

import java.util.List;

public interface ElementoAcervo {
    String nome();
    int contarLivros();
    List<Integer> listarIdsLivros();
    boolean contemLivro(int idLivro);
}
