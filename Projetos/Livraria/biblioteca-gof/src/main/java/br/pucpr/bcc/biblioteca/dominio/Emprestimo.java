package br.pucpr.bcc.biblioteca.dominio;

import java.time.LocalDate;

public class Emprestimo {
    private long idEmprestimo;
    private final int idLivro;
    private final int idEstudante;
    private final LocalDate dataInicio;
    private LocalDate dataVencimento;
    private String estado = "EMPRESTADO";
    private boolean devolvido = false;

    public Emprestimo(int idLivro, int idEstudante, LocalDate dataInicio, LocalDate dataVencimento) {
        this.idLivro = idLivro;
        this.idEstudante = idEstudante;
        this.dataInicio = dataInicio;
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
    public int getIdLivro() { return idLivro; }
    public int getIdEstudante() { return idEstudante; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public String getEstado() { return estado; }
    public boolean isDevolvido() { return devolvido; }

    public void estenderVencimento(int dias) {
        this.dataVencimento = this.dataVencimento.plusDays(dias);
        this.estado = "RENOVADO";
    }

    public void marcarDevolvido() {
        this.devolvido = true;
        this.estado = "DEVOLVIDO";
    }
}
