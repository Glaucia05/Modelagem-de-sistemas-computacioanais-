package br.pucpr.bcc.biblioteca.r02_strategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MultaTetoVinteStrategy implements PoliticaMulta {
    private static final BigDecimal VALOR_DIA = new BigDecimal("1.00");
    private static final BigDecimal TETO = new BigDecimal("20.00");

    @Override
    public String nomePolitica() { return "TETO_20_REAIS"; }

    @Override
    public BigDecimal calcular(LocalDate dataVencimento, LocalDate dataDevolucao) {
        long diasAtraso = Math.max(0L, ChronoUnit.DAYS.between(dataVencimento, dataDevolucao));
        if (diasAtraso == 0) return BigDecimal.ZERO.setScale(2);
        BigDecimal bruto = VALOR_DIA.multiply(BigDecimal.valueOf(diasAtraso));
        return bruto.min(TETO).setScale(2);
    }
}
