package br.pucpr.bcc.biblioteca.eventos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OuvinteInspecao implements OuvinteDevolucao {
    private final List<Integer> filaInspecaoLivros = new ArrayList<>();

    @Override
    public void aoDevolver(EventoDevolucao evento) {
        filaInspecaoLivros.add(evento.idLivro());
    }

    public List<Integer> getFilaInspecaoLivros() {
        return Collections.unmodifiableList(filaInspecaoLivros);
    }
}
