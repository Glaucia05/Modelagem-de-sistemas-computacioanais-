package br.pucpr.bcc.biblioteca.interfaces;

import br.pucpr.bcc.biblioteca.dominio.Pessoa;
import java.util.Optional;

public interface iPessoa {
    Pessoa inserirPessoa(String nome, String sobrenome, String cpf);
    Pessoa atualizarPessoa(String cpf, String novoNome, String novoSobrenome);
    Optional<Pessoa> consultarPessoa(String cpf);
    boolean excluirPessoa(String cpf);
}
