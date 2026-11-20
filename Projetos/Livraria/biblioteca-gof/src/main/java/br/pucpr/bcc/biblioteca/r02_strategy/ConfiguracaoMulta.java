package br.pucpr.bcc.biblioteca.r02_strategy;

import java.util.Objects;

public class ConfiguracaoMulta {
    private PoliticaMulta politicaVigente;

    public ConfiguracaoMulta(PoliticaMulta inicial) {
        this.politicaVigente = Objects.requireNonNull(inicial);
    }

    public PoliticaMulta getPoliticaVigente() { return politicaVigente; }

    public void definirPolitica(PoliticaMulta novaPolitica) {
        this.politicaVigente = Objects.requireNonNull(novaPolitica);
    }
}
