package br.pucpr.bcc.biblioteca.r02_strategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MultaOriginalStrategy implements PoliticaMulta {
    private static final BigDecimal VALOR_DIA = new BigDecimal("1.00");

    @Override
    public String nomePolitica() { return "ORIGINAL_1_REAL_DIA"; }

    @Override
    public BigDecimal calcular(LocalDate dataVencimento, LocalDate dataDevolucao) {
        long diasAtraso = Math.max(0L, ChronoUnit.DAYS.between(dataVencimento, dataDevolucao));
        if (diasAtraso == 0) return BigDecimal.ZERO.setScale(2);
        return VALOR_DIA.multiply(BigDecimal.valueOf(diasAtraso)).setScale(2);
    }
}
