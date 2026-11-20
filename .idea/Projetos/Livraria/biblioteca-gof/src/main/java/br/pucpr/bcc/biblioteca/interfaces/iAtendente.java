package br.pucpr.bcc.biblioteca.interfaces;

import br.pucpr.bcc.biblioteca.dominio.Atendente;
import java.util.Optional;

public interface iAtendente {
    Atendente inserirAtendente(String cpf, int idAtendente);
    Atendente atualizarAtendente(int idAtendente, String novoCpf);
    Optional<Atendente> consultarAtendente(int idAtendente);
    boolean excluirAtendente(int idAtendente);
}
