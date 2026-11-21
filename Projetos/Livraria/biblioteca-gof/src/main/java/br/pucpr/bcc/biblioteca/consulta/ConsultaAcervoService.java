package br.pucpr.bcc.biblioteca.consulta;

import java.util.Optional;

public interface ConsultaAcervoService {
    Optional<MetadadosLivro> consultarMetadados(int idLivro);
    boolean podeReservar(int idLivro);
}
