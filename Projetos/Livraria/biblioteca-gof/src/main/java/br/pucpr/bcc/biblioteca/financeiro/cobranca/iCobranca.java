package br.pucpr.bcc.biblioteca.financeiro.cobranca;

import java.math.BigDecimal;

/** Interface iCobranca do diagrama de componentes (Financeiro requisita, Banco provê). */
public interface iCobranca {
    String registrarCobranca(String idRef, String documentoPagador, BigDecimal valorReais);
}
