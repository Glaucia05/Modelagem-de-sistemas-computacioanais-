package br.pucpr.bcc.biblioteca.r08_abstract_factory;

import br.pucpr.bcc.biblioteca.dominio.Atendente;
import java.util.Optional;

/** GrupoDeColaboradores (Diagrama de Classes de Projeto): coleção de Atendente. */
public interface GrupoDeColaboradores {
    /** Insere ou atualiza o atendente, identificado por idAtendente. */
    void inserirAtendente(Atendente atendente);
    Optional<Atendente> consultarAtendente(int idAtendente);
    void excluirAtendente(int idAtendente);
}
