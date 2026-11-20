package br.pucpr.bcc.biblioteca.r06_command;

import br.pucpr.bcc.biblioteca.r04_facade.ComprovanteOperacao;
import java.time.Instant;

public interface ComandoCirculacao {
    String idComando();
    String autor();
    Instant criadoEm();
    ComprovanteOperacao executar();
}
