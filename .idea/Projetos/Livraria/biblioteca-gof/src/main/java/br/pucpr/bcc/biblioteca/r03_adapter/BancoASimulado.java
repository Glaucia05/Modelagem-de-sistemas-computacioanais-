package br.pucpr.bcc.biblioteca.r03_adapter;

import java.util.HashMap;
import java.util.Map;

public class BancoASimulado {
    public record NotificacaoBancoA(String txId, long centavos, String statusCode) {}
    private final Map<String, Long> cobrancasCentavos = new HashMap<>();

    public String criarBoletoCentavos(String docNumerico, long valorCentavos) {
        String id = "BA-" + (cobrancasCentavos.size() + 1);
        cobrancasCentavos.put(id, valorCentavos);
        return id;
    }

    public long getValorCentavos(String id) {
        return cobrancasCentavos.getOrDefault(id, 0L);
    }
}
