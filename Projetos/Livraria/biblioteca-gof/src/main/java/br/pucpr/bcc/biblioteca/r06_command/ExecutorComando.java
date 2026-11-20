package br.pucpr.bcc.biblioteca.r06_command;

import br.pucpr.bcc.biblioteca.r04_facade.ComprovanteOperacao;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutorComando {
    private final Set<String> idsExecutados = ConcurrentHashMap.newKeySet();
    private final Map<String, ComprovanteOperacao> resultadosPorId = new ConcurrentHashMap<>();

    public ComprovanteOperacao executar(ComandoCirculacao comando) {
        if (idsExecutados.add(comando.idComando())) {
            ComprovanteOperacao resultado = comando.executar();
            resultadosPorId.put(comando.idComando(), resultado);
            return resultado;
        }
        ComprovanteOperacao anterior = resultadosPorId.get(comando.idComando());
        return anterior != null
                ? new ComprovanteOperacao(anterior.sucesso(), anterior.operacao(), anterior.idLivro(),
                        anterior.idEstudante(), anterior.estadoLivroResultante(), anterior.dataVencimento(),
                        "Comando duplicado ignorado: " + comando.idComando())
                : new ComprovanteOperacao(false, "DUPLICADO", 0, 0, null, null, "Comando já executado");
    }
}
