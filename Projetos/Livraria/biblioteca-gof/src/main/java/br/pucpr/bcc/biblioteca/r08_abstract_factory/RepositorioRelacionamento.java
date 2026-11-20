package br.pucpr.bcc.biblioteca.r08_abstract_factory;

import br.pucpr.bcc.biblioteca.dominio.*;
import java.util.Optional;

public interface RepositorioRelacionamento {
    void salvarPessoa(Pessoa pessoa);
    Optional<Pessoa> buscarPessoaPorCpf(String cpf);
    void removerPessoa(String cpf);
    void salvarEstudante(Estudante estudante);
    Optional<Estudante> buscarEstudantePorId(int idEstudante);
    void removerEstudante(int idEstudante);
    void salvarAtendente(Atendente atendente);
    Optional<Atendente> buscarAtendentePorId(int idAtendente);
    void removerAtendente(int idAtendente);
}
