package br.pucpr.bcc.biblioteca.consulta;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CacheMetadadosDecorator extends ConsultaAcervoDecorator {
    private final Map<Integer, MetadadosLivro> cache = new HashMap<>();
    private boolean simularFalhaCache = false;

    public CacheMetadadosDecorator(ConsultaAcervoService proximo) {
        super(proximo);
    }

    public void setSimularFalhaCache(boolean simularFalhaCache) {
        this.simularFalhaCache = simularFalhaCache;
    }

    @Override
    public Optional<MetadadosLivro> consultarMetadados(int idLivro) {
        if (!simularFalhaCache && cache.containsKey(idLivro)) {
            return Optional.ofNullable(cache.get(idLivro));
        }
        Optional<MetadadosLivro> resultado = proximo.consultarMetadados(idLivro);
        if (!simularFalhaCache) {
            resultado.ifPresent(m -> cache.put(idLivro, m));
        }
        return resultado;
    }

    @Override
    public boolean podeReservar(int idLivro) {
        return proximo.podeReservar(idLivro);
    }
}
