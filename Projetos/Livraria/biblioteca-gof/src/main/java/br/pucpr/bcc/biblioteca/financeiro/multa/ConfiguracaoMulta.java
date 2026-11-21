package br.pucpr.bcc.biblioteca.financeiro.multa;

import java.util.Objects;

public class ConfiguracaoMulta {
    private volatile iMulta politicaVigente;

    public ConfiguracaoMulta(iMulta inicial) {
        this.politicaVigente = Objects.requireNonNull(inicial);
    }

    public iMulta getPoliticaVigente() { return politicaVigente; }

    public void definirPolitica(iMulta novaPolitica) {
        this.politicaVigente = Objects.requireNonNull(novaPolitica);
    }
}
