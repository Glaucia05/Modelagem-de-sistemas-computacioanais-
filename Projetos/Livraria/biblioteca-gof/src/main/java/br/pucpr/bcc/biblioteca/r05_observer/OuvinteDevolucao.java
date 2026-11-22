package br.pucpr.bcc.biblioteca.r05_observer;

@FunctionalInterface
public interface OuvinteDevolucao {
    void aoDevolver(EventoDevolucao evento);
}
