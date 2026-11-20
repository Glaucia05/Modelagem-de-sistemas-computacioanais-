package br.pucpr.bcc.biblioteca.r09_decorator;

import br.pucpr.bcc.biblioteca.r08_abstract_factory.RepositorioLivro;
import java.util.Optional;

public class ConsultaAcervoBase implements ConsultaAcervoService {
    private final RepositorioLivro repoLivro;

    public ConsultaAcervoBase(RepositorioLivro repoLivro) {
        this.repoLivro = repoLivro;
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(int idLivro) {
        return repoLivro.buscarPorId(idLivro).map(l -> new MetadadosLivro(l.getIdLivro(), l.getTitulo()));
    }

    @Override
    public boolean podeReservar(int idLivro) {
        return repoLivro.buscarPorId(idLivro)
                .map(l -> "LIVRE".equalsIgnoreCase(l.getEstadoNome()))
                .orElse(false);
    }
}
