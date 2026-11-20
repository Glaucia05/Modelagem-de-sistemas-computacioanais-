package br.pucpr.bcc.biblioteca.r03_adapter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BancoBAdapter implements CobrancaInstitucional {
    private final BancoBSimulado bancoB;
    private final Set<String> quitadas = ConcurrentHashMap.newKeySet();

    public BancoBAdapter(BancoBSimulado bancoB) {
        this.bancoB = bancoB;
    }

    @Override
    public String registrarCobranca(String idRef, String documentoPagador, BigDecimal valorReais) {
        double valor = valorReais.setScale(2, RoundingMode.HALF_UP).doubleValue();
        return bancoB.gerarCobrancaDecimal(documentoPagador, valor);
    }

    @Override
    public Optional<NotificacaoPagamento> processarNotificacao(Object notificacaoBruta) {
        if (!(notificacaoBruta instanceof BancoBSimulado.PayloadBancoB p)) {
            return Optional.empty();
        }
        if ("PAGO".equalsIgnoreCase(p.estadoTexto())) {
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
