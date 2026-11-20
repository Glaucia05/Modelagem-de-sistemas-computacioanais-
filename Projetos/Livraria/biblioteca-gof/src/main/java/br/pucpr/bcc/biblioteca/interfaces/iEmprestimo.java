package br.pucpr.bcc.biblioteca.interfaces;

import br.pucpr.bcc.biblioteca.r04_facade.ComprovanteOperacao;
import java.time.LocalDate;

public interface iEmprestimo {
    ComprovanteOperacao solicitarReserva(String idComando, String autor, int idLivro, int idEstudante, LocalDate data);
    boolean consultarReserva(int idLivro, int idEstudante);
    ComprovanteOperacao efetivarEmprestimo(int idLivro, int idEstudante, LocalDate dataInicio, int diasPrazo);
    ComprovanteOperacao renovarEmprestimo(String idComando, String autor, int idLivro, int idEstudante);
}
