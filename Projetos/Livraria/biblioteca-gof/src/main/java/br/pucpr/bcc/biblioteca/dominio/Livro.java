package br.pucpr.bcc.biblioteca.dominio;

import br.pucpr.bcc.biblioteca.r01_state.*;
import java.util.Objects;

public class Livro {
    private final int idLivro;
    private String titulo;
    private EstadoLivro estado;
    private Integer idReservadoParaEstudante;
    private int numRenovacoes;
    private boolean excluido;

    public Livro(int idLivro, String titulo) {
        this.idLivro = idLivro;
        this.titulo = titulo;
        this.estado = new LivreState();
        this.numRenovacoes = 0;
        this.excluido = false;
    }

    public int getIdLivro() { return idLivro; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getEstadoNome() { return estado.nome(); }
    public EstadoLivro getEstado() { return estado; }
    public Integer getIdReservadoParaEstudante() { return idReservadoParaEstudante; }
    public void setIdReservadoParaEstudante(Integer id) { this.idReservadoParaEstudante = id; }
    public int getNumRenovacoes() { return numRenovacoes; }
    public void setNumRenovacoes(int numRenovacoes) { this.numRenovacoes = numRenovacoes; }
    public void incrementarRenovacoes() { this.numRenovacoes++; }
    public boolean isExcluido() { return excluido; }
    public void marcarExcluido() { this.excluido = true; }

    public void definirEstadoInterno(EstadoLivro novoEstado) {
        this.estado = Objects.requireNonNull(novoEstado);
    }

    public void reconstituirEstadoPorNome(String nomeEstado) {
        switch (nomeEstado.toUpperCase()) {
            case "LIVRE" -> this.estado = new LivreState();
            case "RESERVADO" -> this.estado = new ReservadoState();
            case "EMPRESTADO" -> this.estado = new EmprestadoState();
            case "RENOVADO" -> this.estado = new RenovadoState();
            case "DEVOLVIDO" -> this.estado = new DevolvidoState();
            case "REPARANDO" -> this.estado = new ReparandoState();
            case "INCINERADO" -> this.estado = new IncineradoState();
            default -> throw new IllegalArgumentException("Estado desconhecido: " + nomeEstado);
        }
    }

    public boolean reservar(int idEstudante) { return estado.reservar(this, idEstudante); }
    public boolean emprestar(int idEstudante, Emprestimo emprestimo) { return estado.emprestar(this, idEstudante, emprestimo); }
    public boolean renovar(Emprestimo emprestimo) { return estado.renovar(this, emprestimo); }
    public boolean devolver(Emprestimo emprestimo) { return estado.devolver(this, emprestimo); }
    public boolean inspecionar(boolean bomEstado) { return estado.inspecionar(this, bomEstado); }
    public boolean enviarParaReparo(boolean danoIdentificado) { return estado.enviarParaReparo(this, danoIdentificado); }
    public boolean retomarDoReparo() { return estado.retomarDoReparo(this); }
    public boolean incinerar(boolean semReparo) { return estado.incinerar(this, semReparo); }
    public boolean excluir() { return estado.excluirLivro(this); }

    @Override
    public String toString() {
        return "Livro{id=" + idLivro + ", titulo='" + titulo + "', estado=" + getEstadoNome() + ", renovacoes=" + numRenovacoes + "}";
    }
}
