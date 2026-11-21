package br.pucpr.bcc.biblioteca.persistencia;

public interface UnidadeTrabalho {
    void iniciar();
    void commit();
    void rollback();
    boolean isAtiva();
}
