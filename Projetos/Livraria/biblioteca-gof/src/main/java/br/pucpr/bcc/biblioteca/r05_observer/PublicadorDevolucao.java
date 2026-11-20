package br.pucpr.bcc.biblioteca.r05_observer;

import java.util.ArrayList;
import java.util.List;

public class PublicadorDevolucao {
    private final List<OuvinteDevolucao> ouvintes = new ArrayList<>();

    public void inscrever(OuvinteDevolucao ouvinte) {
        ouvintes.add(ouvinte);
    }

    public void publicar(EventoDevolucao evento) {
        for (OuvinteDevolucao ouvinte : List.copyOf(ouvintes)) {
            try {
                ouvinte.aoDevolver(evento);
            } catch (RuntimeException ex) {
                // Isola falhas de um ouvinte para não afetar os demais nem desfazer a devolução
            }
        }
    }
}
