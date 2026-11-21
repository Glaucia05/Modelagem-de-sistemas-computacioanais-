package br.pucpr.bcc.biblioteca.persistencia;

import br.pucpr.bcc.biblioteca.dominio.Estudante;
import java.util.Optional;

/** GrupoDeAcademicos da modelagem: contém Estudante. {@code inserirEstudante} insere ou substitui (upsert). */
public interface GrupoDeAcademicos {
    void inserirEstudante(Estudante estudante);
    Optional<Estudante> consultarEstudante(int idEstudante);
    void excluirEstudante(int idEstudante);
}
