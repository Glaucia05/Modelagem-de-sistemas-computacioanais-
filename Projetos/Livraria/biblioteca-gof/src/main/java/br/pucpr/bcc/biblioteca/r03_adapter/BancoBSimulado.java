package br.pucpr.bcc.biblioteca.r03_adapter;

import java.util.HashMap;
import java.util.Map;

public class BancoBSimulado {
    public record PayloadBancoB(String codigoExterno, double montanteReais, String estadoTexto) {}
    private final Map<String, Double> cobrancasReais = new HashMap<>();

    public String gerarCobrancaDecimal(String cpfFormatado, double montante) {
        String id = "BB-" + (cobrancasReais.size() + 1);
        cobrancasReais.put(id, montante);
        return id;
    }

    public double getMontanteReais(String id) {
        return cobrancasReais.getOrDefault(id, 0.0);
    }
}
