package br.pucpr.bcc.biblioteca.r03_adapter;

import java.math.BigDecimal;

public record NotificacaoPagamento(String idCobranca, String situacaoPadronizada, BigDecimal valorPago) {}
