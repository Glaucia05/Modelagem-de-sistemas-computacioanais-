package br.pucpr.bcc.biblioteca.r08_abstract_factory;

public interface UnidadeTrabalho {
    void iniciar();
    void commit();
    void rollback();
    boolean isAtiva();
}
