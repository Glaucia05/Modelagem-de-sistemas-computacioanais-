package br.pucpr.bcc.biblioteca.eventos;

@FunctionalInterface
public interface OuvinteDevolucao {
    void aoDevolver(EventoDevolucao evento);
}
