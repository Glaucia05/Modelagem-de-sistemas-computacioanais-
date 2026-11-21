package br.pucpr.bcc.biblioteca.persistencia;

public interface PersistenciaFactory extends AutoCloseable {
    @Override default void close() {}
    String familia();
    UnidadeTrabalho unidadeTrabalho();
    AcervoDeLivros acervoDeLivros();
    Movimentacao movimentacao();
    GruposDoSistema grupos();
}
