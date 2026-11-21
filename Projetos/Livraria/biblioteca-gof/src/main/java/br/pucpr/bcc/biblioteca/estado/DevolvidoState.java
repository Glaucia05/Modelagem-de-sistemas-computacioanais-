package br.pucpr.bcc.biblioteca.estado;

import br.pucpr.bcc.biblioteca.dominio.Livro;

public class DevolvidoState extends AbstractEstadoLivro {
    @Override public String nome() { return "DEVOLVIDO"; }

    @Override
    public boolean inspecionar(Livro livro, boolean bomEstado) {
        if (bomEstado) {
            livro.definirEstadoInterno(new LivreState());
        } else {
            livro.definirEstadoInterno(new ReparandoState());
        }
        return true;
    }
}
