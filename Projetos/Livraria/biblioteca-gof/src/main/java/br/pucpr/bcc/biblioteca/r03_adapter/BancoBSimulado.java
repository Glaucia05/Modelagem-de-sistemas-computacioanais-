package br.pucpr.bcc.biblioteca.r03_adapter;

import java.util.HashMap;
import java.util.Map;

public class BancoBSimulado {
    public record PayloadBancoB(String codigoExterno, double montanteReais, String estadoTexto) {}
    private final Map<String, Double> cobrancasReais = new java.util.concurrent.ConcurrentHashMap<>();

    public synchronized String gerarCobrancaDecimal(String cpfFormatado, double montante) {
        String id = "BB-" + java.util.UUID.randomUUID();
        cobrancasReais.put(id, montante);
        return id;
    }

    public double getMontanteReais(String id) {
        return cobrancasReais.getOrDefault(id, 0.0);
    }
}
