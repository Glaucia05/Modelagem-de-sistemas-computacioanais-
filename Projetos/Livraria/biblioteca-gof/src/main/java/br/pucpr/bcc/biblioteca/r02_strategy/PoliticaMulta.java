package br.pucpr.bcc.biblioteca.r02_strategy;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PoliticaMulta {
    String nomePolitica();
    BigDecimal calcular(LocalDate dataVencimento, LocalDate dataDevolucao);
}
