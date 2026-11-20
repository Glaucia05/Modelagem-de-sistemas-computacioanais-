package br.pucpr.bcc.biblioteca.r03_adapter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BancoAAdapter implements CobrancaInstitucional {
    private final BancoASimulado bancoA;
    private final Set<String> quitadas = ConcurrentHashMap.newKeySet();

    public BancoAAdapter(BancoASimulado bancoA) {
        this.bancoA = bancoA;
    }

    @Override
    public String registrarCobranca(String idRef, String documentoPagador, BigDecimal valorReais) {
        long centavos = valorReais.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
        String docLimpo = documentoPagador.replaceAll("\\D", "");
        return bancoA.criarBoletoCentavos(docLimpo, centavos);
    }

    @Override
    public Optional<NotificacaoPagamento> processarNotificacao(Object notificacaoBruta) {
        if (!(notificacaoBruta instanceof BancoASimulado.NotificacaoBancoA n)) {
            return Optional.empty();
        }
        if ("LIQUIDADO".equalsIgnoreCase(n.statusCode())) {
            boolean primeiraVez = quitadas.add(n.txId());
            if (!primeiraVez) {
                return Optional.empty();
            }
            BigDecimal valor = BigDecimal.valueOf(n.centavos(), 2);
            return Optional.of(new NotificacaoPagamento(n.txId(), "QUITADO", valor));
        }
        return Optional.of(new NotificacaoPagamento(n.txId(), "PENDENTE", BigDecimal.ZERO));
    }

    @Override
    public boolean isQuitada(String idCobranca) {
        return quitadas.contains(idCobranca);
    }
}
