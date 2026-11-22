package br.pucpr.bcc.biblioteca.r09_decorator;

import br.pucpr.bcc.biblioteca.r08_abstract_factory.AcervoDeLivros;
import java.util.Optional;

public class ConsultaAcervoBase implements ConsultaAcervoService {
    private final AcervoDeLivros acervo;

    public ConsultaAcervoBase(AcervoDeLivros acervo) {
        this.acervo = acervo;
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(int idLivro) {
        return acervo.consultarLivro(idLivro).map(l -> new MetadadosLivro(l.getIdLivro(), l.getTitulo()));
    }

    @Override
    public boolean podeReservar(int idLivro) {
        return acervo.consultarLivro(idLivro)
                .map(l -> !l.isExcluido() && "LIVRE".equalsIgnoreCase(l.getEstadoNome()))
                .orElse(false);
    }
}
