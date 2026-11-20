package br.pucpr.bcc.biblioteca.r05_observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OuvinteMulta implements OuvinteDevolucao {
    private final List<EventoDevolucao> devolucoesComAtraso = new ArrayList<>();

    @Override
    public void aoDevolver(EventoDevolucao evento) {
        if (evento.isAtrasada()) {
            devolucoesComAtraso.add(evento);
        }
    }

    public List<EventoDevolucao> getDevolucoesComAtraso() {
        return Collections.unmodifiableList(devolucoesComAtraso);
    }
}
