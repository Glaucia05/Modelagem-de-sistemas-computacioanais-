package br.pucpr.bcc.biblioteca.r02_strategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MultaToleranciaDoisDiasStrategy implements PoliticaMulta {
    private static final BigDecimal VALOR_DIA = new BigDecimal("1.00");
    private static final long TOLERANCIA_DIAS = 2L;

    @Override
    public String nomePolitica() { return "TOLERANCIA_2_DIAS"; }

    @Override
    public BigDecimal calcular(LocalDate dataVencimento, LocalDate dataDevolucao) {
        long diasAtraso = Math.max(0L, ChronoUnit.DAYS.between(dataVencimento, dataDevolucao));
        long diasCobrados = Math.max(0L, diasAtraso - TOLERANCIA_DIAS);
        if (diasCobrados == 0) return BigDecimal.ZERO.setScale(2);
        return VALOR_DIA.multiply(BigDecimal.valueOf(diasCobrados)).setScale(2);
    }
}
