package br.pucpr.bcc.biblioteca.estado;

import br.pucpr.bcc.biblioteca.dominio.Emprestimo;
import br.pucpr.bcc.biblioteca.dominio.Livro;

public class ReservadoState extends AbstractEstadoLivro {
    @Override public String nome() { return "RESERVADO"; }

    @Override
    public boolean emprestar(Livro livro, int idEstudante, Emprestimo emprestimo) {
        if (emprestimo == null || emprestimo.isDevolvido() || emprestimo.getIdLivro() != livro.getIdLivro()
                || emprestimo.getIdEstudante() != idEstudante) return false;
        if (livro.getIdReservadoParaEstudante() == null || livro.getIdReservadoParaEstudante() != idEstudante) {
            return false;
        }
        livro.setIdReservadoParaEstudante(null);
        livro.setNumRenovacoes(0);
        livro.definirEstadoInterno(new EmprestadoState());
        return true;
    }
}
