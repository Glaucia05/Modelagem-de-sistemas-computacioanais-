package br.pucpr.bcc.biblioteca.dominio;

import java.time.LocalDate;

/** Transação de empréstimo: a data da transação é a data de início. */
public class Emprestimo extends Transacao {
    private long idEmprestimo;
    private LocalDate dataVencimento;
    private String estado = "EMPRESTADO";
    private boolean devolvido = false;

    public Emprestimo(int idLivro, int idEstudante, LocalDate dataInicio, LocalDate dataVencimento) {
        super(idLivro, idEstudante, dataInicio);
        this.dataVencimento = dataVencimento;
    }

    public long getIdEmprestimo() { return idEmprestimo; }
    public void definirIdPersistido(long id) {
        if (id <= 0 || (idEmprestimo != 0 && idEmprestimo != id)) throw new IllegalArgumentException("Identificador inválido");
        idEmprestimo = id;
    }
    public void reconstituirSituacao(String estado, boolean devolvido) {
        if (!java.util.Set.of("EMPRESTADO", "RENOVADO", "DEVOLVIDO").contains(estado)
                || devolvido != "DEVOLVIDO".equals(estado)) throw new IllegalArgumentException("Situação de empréstimo inválida");
        this.estado = estado;
        this.devolvido = devolvido;
    }
    public LocalDate getDataInicio() { return getData(); }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public String getEstado() { return estado; }
    /** Operação do Diagrama de Classes de Projeto: {@code consultarEstado() : String}. */
    public String consultarEstado() { return estado; }
    public boolean isDevolvido() { return devolvido; }

    /** Operação do Diagrama de Classes de Projeto: {@code renovar(dias : int)}; estende o vencimento. */
    public void renovar(int dias) {
        this.dataVencimento = this.dataVencimento.plusDays(dias);
        this.estado = "RENOVADO";
    }

    public void marcarDevolvido() {
        this.devolvido = true;
        this.estado = "DEVOLVIDO";
    }
}
