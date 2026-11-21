package br.pucpr.bcc.biblioteca.estado;

import br.pucpr.bcc.biblioteca.dominio.Livro;

public class ReparandoState extends AbstractEstadoLivro {
    @Override public String nome() { return "REPARANDO"; }

    @Override
    public boolean retomarDoReparo(Livro livro) {
        livro.definirEstadoInterno(new LivreState());
        return true;
    }

    @Override
    public boolean incinerar(Livro livro, boolean semReparo) {
        if (!semReparo) return false;
        livro.definirEstadoInterno(new IncineradoState());
        return true;
    }
}
