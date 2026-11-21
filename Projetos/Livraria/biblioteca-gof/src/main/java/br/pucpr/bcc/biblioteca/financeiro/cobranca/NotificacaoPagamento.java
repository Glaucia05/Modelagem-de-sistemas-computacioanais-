package br.pucpr.bcc.biblioteca.financeiro.cobranca;

import java.math.BigDecimal;

public record NotificacaoPagamento(String idCobranca, String situacaoPadronizada, BigDecimal valorPago) {}
