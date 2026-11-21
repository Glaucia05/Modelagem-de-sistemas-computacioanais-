package br.pucpr.bcc.biblioteca.financeiro.multa;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface iMulta {
    String nomePolitica();
    BigDecimal calcular(LocalDate dataVencimento, LocalDate dataDevolucao);
}
