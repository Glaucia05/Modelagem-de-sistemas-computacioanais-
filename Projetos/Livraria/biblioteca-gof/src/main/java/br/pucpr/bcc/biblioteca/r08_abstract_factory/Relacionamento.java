package br.pucpr.bcc.biblioteca.r08_abstract_factory;

/**
 * Ponto interno de acesso "Relacionamento" (Trabalho 02): reúne os três grupos do modelo de classes
 * (GrupoDePessoas, GrupoDeAcademicos e GrupoDeColaboradores). Produto da Abstract Factory (R08).
 */
public interface Relacionamento extends GrupoDePessoas, GrupoDeAcademicos, GrupoDeColaboradores {
    default UnidadeTrabalho unidadeTrabalho() { return null; }
}
