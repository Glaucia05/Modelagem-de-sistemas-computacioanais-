package br.pucpr.bcc.biblioteca.r09_decorator;

public abstract class ConsultaAcervoDecorator implements ConsultaAcervoService {
    protected final ConsultaAcervoService proximo;

    protected ConsultaAcervoDecorator(ConsultaAcervoService proximo) {
        this.proximo = proximo;
    }
}
