package br.pucpr.bcc.biblioteca.r05_observer;

import java.time.LocalDate;

public record EventoDevolucao(
        int idLivro,
        int idEstudante,
        LocalDate dataVencimento,
        LocalDate dataDevolucao
) {
    public boolean isAtrasada() {
        return dataDevolucao.isAfter(dataVencimento);
    }
}
