package br.pucpr.bcc.biblioteca.r07_template;

import br.pucpr.bcc.biblioteca.dominio.Pessoa;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.Relacionamento;
import java.util.Optional;

public class CadastroPessoaTemplate extends RotinaCadastroTemplate<Pessoa> {
    private final Relacionamento repo;
    private final String nome;
    private final String sobrenome;
    private final String cpf;

    public CadastroPessoaTemplate(Relacionamento repo, String nome, String sobrenome, String cpf) {
        this.repo = repo;
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.cpf = cpf;
    }

    @Override protected Optional<Pessoa> buscarExistente() { return cpf == null ? Optional.empty() : repo.consultarPessoa(cpf); }

    @Override
    protected void validar() {
        if (cpf == null || cpf.isBlank() || nome == null || nome.isBlank() || sobrenome == null) {
            throw new IllegalArgumentException("Dados obrigatórios de Pessoa ausentes");
        }
    }

    @Override protected void verificarPreRequisitos() {}
    @Override protected Pessoa criar() { return new Pessoa(nome, sobrenome, cpf); }
    @Override protected void armazenar(Pessoa entidade) { repo.inserirPessoa(entidade); }
}
