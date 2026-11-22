package br.pucpr.bcc.biblioteca.r08_abstract_factory;

import br.pucpr.bcc.biblioteca.dominio.Pessoa;
import java.util.Optional;

/** GrupoDePessoas (Diagrama de Classes de Projeto): coleção de Pessoa cadastrada no RH. */
public interface GrupoDePessoas {
    /** Insere ou atualiza a pessoa, identificada pelo CPF. */
    void inserirPessoa(Pessoa pessoa);
    Optional<Pessoa> consultarPessoa(String cpf);
    void excluirPessoa(String cpf);
}
