package br.pucpr.bcc.biblioteca.interfaces;

import br.pucpr.bcc.biblioteca.r04_facade.ComprovanteOperacao;
import java.time.LocalDate;

public interface iEmprestimo {
    ComprovanteOperacao solicitarReserva(String idComando, String autor, int idLivro, int idEstudante, LocalDate data);
    boolean consultarReserva(int idLivro, int idEstudante);
    ComprovanteOperacao efetivarEmprestimo(int idLivro, int idEstudante, LocalDate dataInicio, int diasPrazo);
    ComprovanteOperacao renovarEmprestimo(String idComando, String autor, int idLivro, int idEstudante);

    // Nomes do Diagrama de Classes de Projeto (TratadorDeEmprestimo). Os parâmetros idComando/autor/data
    // acompanham o requisito R06 (Command); as regras continuam no domínio.
    default ComprovanteOperacao solicitarReservaLivro(String idComando, String autor, int idLivro, int idEstudante, LocalDate data) {
        return solicitarReserva(idComando, autor, idLivro, idEstudante, data);
    }
    default boolean consultarReservaLivro(int idLivro, int idEstudante) { return consultarReserva(idLivro, idEstudante); }
    default ComprovanteOperacao emprestarLivro(int idLivro, int idEstudante, LocalDate dataInicio, int diasPrazo) {
        return efetivarEmprestimo(idLivro, idEstudante, dataInicio, diasPrazo);
    }
    default ComprovanteOperacao renovarEmprestimoLivro(String idComando, String autor, int idLivro, int idEstudante) {
        return renovarEmprestimo(idComando, autor, idLivro, idEstudante);
    }
}
