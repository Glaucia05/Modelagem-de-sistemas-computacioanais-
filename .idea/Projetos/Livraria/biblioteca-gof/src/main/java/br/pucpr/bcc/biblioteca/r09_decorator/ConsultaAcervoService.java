package br.pucpr.bcc.biblioteca.r09_decorator;

import java.util.Optional;

public interface ConsultaAcervoService {
    Optional<MetadadosLivro> consultarMetadados(int idLivro);
    boolean podeReservar(int idLivro);
}
