package br.pucpr.bcc.biblioteca.r08_abstract_factory;

import br.pucpr.bcc.biblioteca.dominio.Estudante;
import java.util.Optional;

/** GrupoDeAcademicos (Diagrama de Classes de Projeto): coleção de Estudante do Acadêmico. */
public interface GrupoDeAcademicos {
    /** Insere ou atualiza o estudante, identificado por idEstudante. */
    void inserirEstudante(Estudante estudante);
    Optional<Estudante> consultarEstudante(int idEstudante);
    void excluirEstudante(int idEstudante);
}
