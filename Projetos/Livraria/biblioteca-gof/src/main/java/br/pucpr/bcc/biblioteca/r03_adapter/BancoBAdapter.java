package br.pucpr.bcc.biblioteca.r03_adapter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BancoBAdapter implements CobrancaInstitucional {
    private final BancoBSimulado bancoB;
    private final java.util.Map<String, BigDecimal> valoresEmitidos = new ConcurrentHashMap<>();
    private final Set<String> quitadas = ConcurrentHashMap.newKeySet();

    public BancoBAdapter(BancoBSimulado bancoB) {
        this.bancoB = bancoB;
    }

    @Override
    public String registrarCobranca(String idRef, String documentoPagador, BigDecimal valorReais) {
        double valor = valorReais.setScale(2, RoundingMode.HALF_UP).doubleValue();
        String digitos = documentoPagador.replaceAll("\\D", "");
        if (digitos.length() != 11 || !Double.isFinite(valor) || valor <= 0) throw new IllegalArgumentException("Valor ou CPF inválido");
        String formatado = digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-" + digitos.substring(9);
        String id = bancoB.gerarCobrancaDecimal(formatado, valor);
        valoresEmitidos.put(id, valorReais.setScale(2, RoundingMode.HALF_UP));
        return id;
    }

    @Override
    public Optional<NotificacaoPagamento> processarNotificacao(Object notificacaoBruta) {
        if (!(notificacaoBruta instanceof BancoBSimulado.PayloadBancoB p)) {
            return Optional.empty();
        }
        if (p.codigoExterno() == null) return Optional.empty();
        BigDecimal esperado = valoresEmitidos.get(p.codigoExterno());
        if (esperado == null || quitadas.contains(p.codigoExterno()) || !Double.isFinite(p.montanteReais())) return Optional.empty();
        if ("PAGO".equalsIgnoreCase(p.estadoTexto())) {
            if (esperado.compareTo(BigDecimal.valueOf(p.montanteReais()).setScale(2, RoundingMode.HALF_UP)) != 0) return Optional.empty();
            boolean primeiraVez = quitadas.add(p.codigoExterno());
            if (!primeiraVez) {
                return Optional.empty();
            }
            BigDecimal valor = BigDecimal.valueOf(p.montanteReais()).setScale(2, RoundingMode.HALF_UP);
            return Optional.of(new NotificacaoPagamento(p.codigoExterno(), "QUITADO", valor));
        }
        return Optional.of(new NotificacaoPagamento(p.codigoExterno(), "PENDENTE", BigDecimal.ZERO));
    }

    @Override
    public boolean isQuitada(String idCobranca) {
        return quitadas.contains(idCobranca);
    }
}
