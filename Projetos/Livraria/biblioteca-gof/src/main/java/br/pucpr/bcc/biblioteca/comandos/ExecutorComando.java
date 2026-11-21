package br.pucpr.bcc.biblioteca.comandos;

import br.pucpr.bcc.biblioteca.circulacao.ComprovanteOperacao;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExecutorComando {
    private final Map<String, ComprovanteOperacao> resultadosPorId = new HashMap<>();

    // A execução e o registro formam uma única seção crítica. A repetição aguarda
    // o resultado original; exceções não deixam o identificador preso no executor.
    public synchronized ComprovanteOperacao executar(ComandoCirculacao comando) {
        Objects.requireNonNull(comando);
        if (comando.idComando() == null || comando.idComando().isBlank()
                || comando.autor() == null || comando.autor().isBlank() || comando.criadoEm() == null) {
            throw new IllegalArgumentException("Comando exige identificador, autor e instante");
        }
        return resultadosPorId.computeIfAbsent(comando.idComando(),
                id -> Objects.requireNonNull(comando.executar()));
    }
}
