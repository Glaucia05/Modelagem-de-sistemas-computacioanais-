package br.pucpr.bcc.biblioteca.dominio;

import java.math.BigDecimal;

public record BoletoMulta(
        String idCobranca,
        int idLivro,
        int idEstudante,
        BigDecimal valor,
        String politicaAplicada,
        String situacao
) {}
