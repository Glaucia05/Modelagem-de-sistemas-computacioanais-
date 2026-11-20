package br.pucpr.bcc.biblioteca.interfaces;

import br.pucpr.bcc.biblioteca.dominio.Estudante;
import java.util.Optional;

public interface iEstudante {
    Estudante inserirEstudante(String cpf, int idEstudante, String matricula);
    Estudante atualizarEstudante(int idEstudante, String cpf, String novaMatricula);
    Optional<Estudante> consultarEstudante(int idEstudante);
    boolean excluirEstudante(int idEstudante);
}
