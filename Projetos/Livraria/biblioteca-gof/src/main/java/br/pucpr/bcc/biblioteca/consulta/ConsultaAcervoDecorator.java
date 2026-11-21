package br.pucpr.bcc.biblioteca.consulta;

public abstract class ConsultaAcervoDecorator implements ConsultaAcervoService {
    protected final ConsultaAcervoService proximo;

    protected ConsultaAcervoDecorator(ConsultaAcervoService proximo) {
        this.proximo = proximo;
    }
}
