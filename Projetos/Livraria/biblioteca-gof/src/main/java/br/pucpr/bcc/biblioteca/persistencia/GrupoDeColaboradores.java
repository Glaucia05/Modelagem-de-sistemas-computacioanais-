package br.pucpr.bcc.biblioteca.persistencia;

import br.pucpr.bcc.biblioteca.dominio.Atendente;
import java.util.Optional;

/** GrupoDeColaboradores da modelagem: contém Atendente. {@code inserirAtendente} insere ou substitui (upsert). */
public interface GrupoDeColaboradores {
    void inserirAtendente(Atendente atendente);
    Optional<Atendente> consultarAtendente(int idAtendente);
    void excluirAtendente(int idAtendente);
}
