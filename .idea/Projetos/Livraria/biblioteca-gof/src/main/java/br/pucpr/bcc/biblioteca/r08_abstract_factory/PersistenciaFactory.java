package br.pucpr.bcc.biblioteca.r08_abstract_factory;

public interface PersistenciaFactory {
    String familia();
    UnidadeTrabalho unidadeTrabalho();
    RepositorioLivro repositorioLivro();
    RepositorioMovimentacao repositorioMovimentacao();
    RepositorioRelacionamento repositorioRelacionamento();
}
