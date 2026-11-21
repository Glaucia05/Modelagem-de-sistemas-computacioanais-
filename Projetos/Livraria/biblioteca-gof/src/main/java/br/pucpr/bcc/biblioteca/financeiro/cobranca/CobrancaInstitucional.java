package br.pucpr.bcc.biblioteca.financeiro.cobranca;

/** Contrato institucional de cobrança: reúne iCobranca e iQuitacao; cada banco tem seu adaptador. */
public interface CobrancaInstitucional extends iCobranca, iQuitacao {
}
