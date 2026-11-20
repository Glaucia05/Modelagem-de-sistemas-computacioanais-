package br.pucpr.bcc.biblioteca.r05_observer;

import java.util.concurrent.atomic.AtomicInteger;

public class OuvinteIndicadores implements OuvinteDevolucao {
    private final AtomicInteger totalDevolucoes = new AtomicInteger(0);
    private final AtomicInteger totalAtrasadas = new AtomicInteger(0);

    @Override
    public void aoDevolver(EventoDevolucao evento) {
        totalDevolucoes.incrementAndGet();
        if (evento.isAtrasada()) {
            totalAtrasadas.incrementAndGet();
        }
    }

    public int getTotalDevolucoes() { return totalDevolucoes.get(); }
    public int getTotalAtrasadas() { return totalAtrasadas.get(); }
}
