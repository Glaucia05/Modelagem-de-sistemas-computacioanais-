package br.pucpr.bcc.biblioteca.r01_state;

import br.pucpr.bcc.biblioteca.dominio.Livro;

public class IncineradoState extends AbstractEstadoLivro {
    @Override public String nome() { return "INCINERADO"; }

    @Override
    public boolean excluirLivro(Livro livro) {
        livro.marcarExcluido();
        return true;
    }
}
