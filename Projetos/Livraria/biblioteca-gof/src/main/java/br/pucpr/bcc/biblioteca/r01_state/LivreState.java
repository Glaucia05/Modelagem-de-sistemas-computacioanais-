package br.pucpr.bcc.biblioteca.r01_state;

import br.pucpr.bcc.biblioteca.dominio.Livro;

public class LivreState extends AbstractEstadoLivro {
    @Override public String nome() { return "LIVRE"; }

    @Override
    public boolean reservar(Livro livro, int idEstudante) {
        if (livro.isExcluido() || idEstudante <= 0) return false;
        livro.setIdReservadoParaEstudante(idEstudante);
        livro.definirEstadoInterno(new ReservadoState());
        return true;
    }

    @Override
    public boolean enviarParaReparo(Livro livro, boolean danoIdentificado) {
        if (!danoIdentificado) return false;
        livro.definirEstadoInterno(new ReparandoState());
        return true;
    }

    @Override
    public boolean excluirLivro(Livro livro) {
        livro.marcarExcluido();
        return true;
    }
}
