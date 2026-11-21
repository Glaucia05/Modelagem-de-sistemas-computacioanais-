package br.pucpr.bcc.biblioteca.persistencia;

/**
 * Ponto interno de acesso aos grupos de pessoas (antigo "Relacionamento" do enunciado):
 * reúne GrupoDePessoas, GrupoDeAcademicos e GrupoDeColaboradores da modelagem.
 */
public interface GruposDoSistema extends GrupoDePessoas, GrupoDeAcademicos, GrupoDeColaboradores {
    default UnidadeTrabalho unidadeTrabalho() { return null; }
}
