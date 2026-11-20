package br.pucpr.bcc.biblioteca.r07_template;

import br.pucpr.bcc.biblioteca.dominio.Atendente;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.RepositorioRelacionamento;
import java.util.Optional;

public class CadastroAtendenteTemplate extends RotinaCadastroTemplate<Atendente> {
    private final RepositorioRelacionamento repo;
    private final int idAtendente;
    private final String cpf;

    public CadastroAtendenteTemplate(RepositorioRelacionamento repo, int idAtendente, String cpf) {
        this.repo = repo;
        this.idAtendente = idAtendente;
        this.cpf = cpf;
    }

    @Override protected Optional<Atendente> buscarExistente() { return repo.buscarAtendentePorId(idAtendente); }

    @Override
    protected void validar() {
        if (idAtendente <= 0 || cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("Dados inválidos de Atendente");
        }
    }

    @Override
    protected void verificarPreRequisitos() {
        if (repo.buscarPessoaPorCpf(cpf).isEmpty()) {
            throw new IllegalStateException("CPF não cadastrado para Atendente: " + cpf);
        }
    }

    @Override protected Atendente criar() { return new Atendente(idAtendente, cpf); }
    @Override protected void armazenar(Atendente entidade) { repo.salvarAtendente(entidade); }
}
