package br.pucpr.bcc.biblioteca.r04_facade;

import java.time.LocalDate;

public record ComprovanteOperacao(
        boolean sucesso,
        String operacao,
        int idLivro,
        int idEstudante,
        String estadoLivroResultante,
        LocalDate dataVencimento,
        String mensagem
) {}
