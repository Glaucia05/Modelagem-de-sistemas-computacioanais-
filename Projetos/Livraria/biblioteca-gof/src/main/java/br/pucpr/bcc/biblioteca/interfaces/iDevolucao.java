package br.pucpr.bcc.biblioteca.interfaces;

import br.pucpr.bcc.biblioteca.dominio.BoletoMulta;
import br.pucpr.bcc.biblioteca.r04_facade.ComprovanteOperacao;
import java.time.LocalDate;
import java.util.Optional;

public interface iDevolucao {
    ComprovanteOperacao registrarDevolucao(int idLivro, int idEstudante, LocalDate dataDevolucao);
    Optional<BoletoMulta> emitirBoletoMulta(int idLivro, int idEstudante);
    ComprovanteOperacao inspecionarLivro(int idLivro, boolean bomEstado);
    ComprovanteOperacao emitirLaudoIrrecuperavel(int idLivro);

    // Nome do Diagrama de Classes de Projeto (TratadorDeDevolucao).
    default ComprovanteOperacao registrarDevolucaoLivro(int idLivro, int idEstudante, LocalDate dataDevolucao) {
        return registrarDevolucao(idLivro, idEstudante, dataDevolucao);
    }
}
