package br.pucpr.bcc.biblioteca.tratadores;

import br.pucpr.bcc.biblioteca.dominio.*;
import br.pucpr.bcc.biblioteca.interfaces.*;
import br.pucpr.bcc.biblioteca.r07_template.*;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.*;

import java.util.Optional;

public class TratadorDeCadastro implements iLivro, iPessoa, iEstudante, iAtendente {
    private final RepositorioLivro repoLivro;
    private final RepositorioRelacionamento repoRel;

    public TratadorDeCadastro(RepositorioLivro repoLivro, RepositorioRelacionamento repoRel) {
        this.repoLivro = repoLivro;
        this.repoRel = repoRel;
    }

    @Override
    public Pessoa inserirPessoa(String nome, String sobrenome, String cpf) {
        return new CadastroPessoaTemplate(repoRel, nome, sobrenome, cpf).incluir().entidade();
    }

    @Override
    public Pessoa atualizarPessoa(String cpf, String novoNome, String novoSobrenome) {
        Pessoa atualizada = new Pessoa(novoNome, novoSobrenome, cpf);
        repoRel.salvarPessoa(atualizada);
        return atualizada;
    }

    @Override
    public Optional<Pessoa> consultarPessoa(String cpf) {
        return repoRel.buscarPessoaPorCpf(cpf);
    }

    @Override
    public boolean excluirPessoa(String cpf) {
        if (repoRel.buscarPessoaPorCpf(cpf).isEmpty()) return false;
        repoRel.removerPessoa(cpf);
        return true;
    }

    @Override
    public Estudante inserirEstudante(String cpf, int idEstudante, String matricula) {
        return new CadastroEstudanteTemplate(repoRel, idEstudante, cpf, matricula).incluir().entidade();
    }

    public RotinaCadastroTemplate.ResultadoCadastro<Estudante> inserirEstudanteComResultado(int idEstudante, String cpf, String matricula) {
        return new CadastroEstudanteTemplate(repoRel, idEstudante, cpf, matricula).incluir();
    }

    @Override
    public Estudante atualizarEstudante(int idEstudante, String cpf, String novaMatricula) {
        if (repoRel.buscarPessoaPorCpf(cpf).isEmpty()) {
            throw new IllegalStateException("CPF não cadastrado em Pessoa/RH: " + cpf);
        }
        Estudante atualizado = new Estudante(idEstudante, cpf, novaMatricula);
        repoRel.salvarEstudante(atualizado);
        return atualizado;
    }

    @Override
    public Optional<Estudante> consultarEstudante(int idEstudante) {
        return repoRel.buscarEstudantePorId(idEstudante);
    }

    @Override
    public boolean excluirEstudante(int idEstudante) {
        if (repoRel.buscarEstudantePorId(idEstudante).isEmpty()) return false;
        repoRel.removerEstudante(idEstudante);
        return true;
    }

    @Override
    public Atendente inserirAtendente(String cpf, int idAtendente) {
        return new CadastroAtendenteTemplate(repoRel, idAtendente, cpf).incluir().entidade();
    }

    @Override
    public Atendente atualizarAtendente(int idAtendente, String novoCpf) {
        if (repoRel.buscarPessoaPorCpf(novoCpf).isEmpty()) {
            throw new IllegalStateException("CPF não cadastrado para Atendente: " + novoCpf);
        }
        Atendente atualizado = new Atendente(idAtendente, novoCpf);
        repoRel.salvarAtendente(atualizado);
        return atualizado;
    }

    @Override
    public Optional<Atendente> consultarAtendente(int idAtendente) {
        return repoRel.buscarAtendentePorId(idAtendente);
    }

    @Override
    public boolean excluirAtendente(int idAtendente) {
        if (repoRel.buscarAtendentePorId(idAtendente).isEmpty()) return false;
        repoRel.removerAtendente(idAtendente);
        return true;
    }

    @Override
    public Livro inserirLivro(int idLivro, String titulo) {
        return new CadastroLivroTemplate(repoLivro, idLivro, titulo).incluir().entidade();
    }

    @Override
    public Livro atualizarLivro(int idLivro, String novoTitulo) {
        Livro livro = repoLivro.buscarPorId(idLivro).orElseThrow(() -> new IllegalArgumentException("Livro não encontrado"));
        livro.setTitulo(novoTitulo);
        repoLivro.salvar(livro);
        return livro;
    }

    @Override
    public Optional<Livro> consultarLivro(int idLivro) {
        return repoLivro.buscarPorId(idLivro);
    }

    @Override
    public boolean excluirLivro(int idLivro) {
        Optional<Livro> opt = repoLivro.buscarPorId(idLivro);
        if (opt.isEmpty()) return false;
        Livro livro = opt.get();
        if (!livro.excluir()) return false;
        repoLivro.salvar(livro);
        return true;
    }
}
