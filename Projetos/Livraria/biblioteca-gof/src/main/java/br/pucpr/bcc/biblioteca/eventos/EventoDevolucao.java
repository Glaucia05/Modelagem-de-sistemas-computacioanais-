package br.pucpr.bcc.biblioteca.eventos;

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
