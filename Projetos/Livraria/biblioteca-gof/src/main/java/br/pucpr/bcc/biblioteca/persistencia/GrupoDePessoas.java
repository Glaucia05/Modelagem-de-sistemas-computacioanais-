package br.pucpr.bcc.biblioteca.persistencia;

import br.pucpr.bcc.biblioteca.dominio.Pessoa;
import java.util.Optional;

/** GrupoDePessoas da modelagem: contém Pessoa. {@code inserirPessoa} insere ou substitui (upsert). */
public interface GrupoDePessoas {
    void inserirPessoa(Pessoa pessoa);
    Optional<Pessoa> consultarPessoa(String cpf);
    void excluirPessoa(String cpf);
}
