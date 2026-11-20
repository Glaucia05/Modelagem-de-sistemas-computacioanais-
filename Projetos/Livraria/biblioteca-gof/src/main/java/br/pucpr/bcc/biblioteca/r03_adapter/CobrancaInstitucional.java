package br.pucpr.bcc.biblioteca.r03_adapter;

import java.math.BigDecimal;
import java.util.Optional;

public interface CobrancaInstitucional {
    String registrarCobranca(String idRef, String documentoPagador, BigDecimal valorReais);
    Optional<NotificacaoPagamento> processarNotificacao(Object notificacaoBruta);
    boolean isQuitada(String idCobranca);
}
