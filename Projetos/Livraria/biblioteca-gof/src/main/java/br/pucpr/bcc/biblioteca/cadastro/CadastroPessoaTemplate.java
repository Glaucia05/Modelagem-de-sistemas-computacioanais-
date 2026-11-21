package br.pucpr.bcc.biblioteca.cadastro;

import br.pucpr.bcc.biblioteca.dominio.Pessoa;
import br.pucpr.bcc.biblioteca.persistencia.GruposDoSistema;
import java.util.Optional;

public class CadastroPessoaTemplate extends RotinaCadastroTemplate<Pessoa> {
    private final GruposDoSistema repo;
    private final String nome;
    private final String sobrenome;
    private final String cpf;

    public CadastroPessoaTemplate(GruposDoSistema repo, String nome, String sobrenome, String cpf) {
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
