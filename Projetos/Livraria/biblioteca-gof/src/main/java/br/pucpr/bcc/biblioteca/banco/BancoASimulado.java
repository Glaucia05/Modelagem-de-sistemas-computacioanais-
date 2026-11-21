package br.pucpr.bcc.biblioteca.banco;

import java.util.HashMap;
import java.util.Map;

public class BancoASimulado {
    public record NotificacaoBancoA(String txId, long centavos, String statusCode) {}
    private final Map<String, Long> cobrancasCentavos = new java.util.concurrent.ConcurrentHashMap<>();

    public synchronized String criarBoletoCentavos(String docNumerico, long valorCentavos) {
        String id = "BA-" + java.util.UUID.randomUUID();
        cobrancasCentavos.put(id, valorCentavos);
        return id;
    }

    public long getValorCentavos(String id) {
        return cobrancasCentavos.getOrDefault(id, 0L);
    }
}
