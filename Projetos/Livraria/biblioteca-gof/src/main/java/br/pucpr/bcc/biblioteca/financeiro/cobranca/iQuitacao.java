package br.pucpr.bcc.biblioteca.financeiro.cobranca;

import java.util.Optional;

/** Interface iQuitacao do diagrama de componentes (notificações de pagamento do Banco). */
public interface iQuitacao {
    Optional<NotificacaoPagamento> processarNotificacao(Object notificacaoBruta);
    boolean isQuitada(String idCobranca);
}
