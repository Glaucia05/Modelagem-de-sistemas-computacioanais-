package br.pucpr.bcc.biblioteca.persistencia;

public class ProvedorPersistencia {
    public static PersistenciaFactory criarPorPerfil(String perfil) {
        if ("test".equalsIgnoreCase(perfil) || "memoria".equalsIgnoreCase(perfil)) {
            return new MemoriaPersistenciaFactory();
        }
        if ("prod".equalsIgnoreCase(perfil) || "mysql".equalsIgnoreCase(perfil)) {
            return new MysqlPersistenciaFactory();
        }
        throw new IllegalArgumentException("Perfil de persistência inválido: " + perfil);
    }
}
