package br.pucpr.bcc.biblioteca.r08_abstract_factory;

public interface PersistenciaFactory extends AutoCloseable {
    @Override default void close() {}
    String familia();
    UnidadeTrabalho unidadeTrabalho();
    RepositorioLivro repositorioLivro();
    RepositorioMovimentacao repositorioMovimentacao();
    RepositorioRelacionamento repositorioRelacionamento();
}
