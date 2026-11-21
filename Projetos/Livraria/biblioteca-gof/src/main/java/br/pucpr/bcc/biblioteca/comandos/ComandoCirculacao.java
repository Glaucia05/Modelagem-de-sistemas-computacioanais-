package br.pucpr.bcc.biblioteca.comandos;

import br.pucpr.bcc.biblioteca.circulacao.ComprovanteOperacao;
import java.time.Instant;

public interface ComandoCirculacao {
    String idComando();
    String autor();
    Instant criadoEm();
    ComprovanteOperacao executar();
}
