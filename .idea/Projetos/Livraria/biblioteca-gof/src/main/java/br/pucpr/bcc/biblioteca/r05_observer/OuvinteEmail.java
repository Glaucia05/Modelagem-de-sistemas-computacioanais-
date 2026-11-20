package br.pucpr.bcc.biblioteca.r05_observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OuvinteEmail implements OuvinteDevolucao {
    private final List<String> mensagensEnviadas = new ArrayList<>();
    private boolean simularFalhaSmtp = false;

    public void setSimularFalhaSmtp(boolean simularFalhaSmtp) {
        this.simularFalhaSmtp = simularFalhaSmtp;
    }

    @Override
    public void aoDevolver(EventoDevolucao evento) {
        if (simularFalhaSmtp) {
            throw new RuntimeException("Falha de conexão SMTP ao enviar comprovante de devolução");
        }
        mensagensEnviadas.add("Comprovante enviado ao estudante " + evento.idEstudante() + " (livro " + evento.idLivro() + ")");
    }

    public List<String> getMensagensEnviadas() {
        return Collections.unmodifiableList(mensagensEnviadas);
    }
}
