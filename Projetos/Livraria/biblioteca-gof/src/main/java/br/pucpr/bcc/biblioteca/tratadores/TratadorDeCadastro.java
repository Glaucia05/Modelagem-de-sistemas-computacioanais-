package br.pucpr.bcc.biblioteca.tratadores;

import br.pucpr.bcc.biblioteca.dominio.*;
import br.pucpr.bcc.biblioteca.interfaces.*;
import br.pucpr.bcc.biblioteca.r07_template.*;
import br.pucpr.bcc.biblioteca.r08_abstract_factory.*;

import java.util.Optional;

public class TratadorDeCadastro implements iLivro, iPessoa, iEstudante, iAtendente {
    private final AcervoDeLivros acervo;
    private final Relacionamento relacionamento;

    public TratadorDeCadastro(AcervoDeLivros acervo, Relacionamento relacionamento) {
        this.acervo = acervo;
        this.relacionamento = relacionamento;
    }

    @Override
    public Pessoa inserirPessoa(String nome, String sobrenome, String cpf) {
        return new CadastroPessoaTemplate(relacionamento, nome, sobrenome, cpf).incluir().entidade();
    }

    @Override
    public Pessoa atualizarPessoa(String cpf, String novoNome, String novoSobrenome) {
        Pessoa atualizada = new Pessoa(novoNome, novoSobrenome, cpf);
        relacionamento.inserirPessoa(atualizada);
        return atualizada;
    }

    @Override
    public Optional<Pessoa> consultarPessoa(String cpf) {
        return relacionamento.consultarPessoa(cpf);
    }

    @Override
    public boolean excluirPessoa(String cpf) {
        if (relacionamento.consultarPessoa(cpf).isEmpty()) return false;
        relacionamento.excluirPessoa(cpf);
        return true;
    }

    @Override
    public Estudante inserirEstudante(String cpf, int idEstudante, String matricula) {
        return new CadastroEstudanteTemplate(relacionamento, idEstudante, cpf, matricula).incluir().entidade();
    }

    public RotinaCadastroTemplate.ResultadoCadastro<Estudante> inserirEstudanteComResultado(int idEstudante, String cpf, String matricula) {
        return new CadastroEstudanteTemplate(relacionamento, idEstudante, cpf, matricula).incluir();
    }

    @Override
    public Estudante atualizarEstudante(int idEstudante, String cpf, String novaMatricula) {
        if (relacionamento.consultarPessoa(cpf).isEmpty()) {
            throw new IllegalStateException("CPF não cadastrado em Pessoa/RH: " + cpf);
        }
        Estudante atualizado = new Estudante(idEstudante, cpf, novaMatricula);
        relacionamento.inserirEstudante(atualizado);
        return atualizado;
    }

    @Override
    public Optional<Estudante> consultarEstudante(int idEstudante) {
        return relacionamento.consultarEstudante(idEstudante);
    }

    @Override
    public boolean excluirEstudante(int idEstudante) {
        if (relacionamento.consultarEstudante(idEstudante).isEmpty()) return false;
        relacionamento.excluirEstudante(idEstudante);
        return true;
    }

    @Override
    public Atendente inserirAtendente(String cpf, int idAtendente) {
        return new CadastroAtendenteTemplate(relacionamento, idAtendente, cpf).incluir().entidade();
    }

    @Override
    public Atendente atualizarAtendente(int idAtendente, String novoCpf) {
        if (relacionamento.consultarPessoa(novoCpf).isEmpty()) {
            throw new IllegalStateException("CPF não cadastrado para Atendente: " + novoCpf);
        }
        Atendente atualizado = new Atendente(idAtendente, novoCpf);
        relacionamento.inserirAtendente(atualizado);
        return atualizado;
    }

    @Override
    public Optional<Atendente> consultarAtendente(int idAtendente) {
        return relacionamento.consultarAtendente(idAtendente);
    }

    @Override
    public boolean excluirAtendente(int idAtendente) {
        if (relacionamento.consultarAtendente(idAtendente).isEmpty()) return false;
        relacionamento.excluirAtendente(idAtendente);
        return true;
    }

    @Override
    public Livro inserirLivro(int idLivro, String titulo) {
        return new CadastroLivroTemplate(acervo, idLivro, titulo).incluir().entidade();
    }

    @Override
    public Livro atualizarLivro(int idLivro, String novoTitulo) {
        Livro livro = acervo.consultarLivro(idLivro).orElseThrow(() -> new IllegalArgumentException("Livro não encontrado"));
        livro.setTitulo(novoTitulo);
        acervo.salvar(livro);
        return livro;
    }

    @Override
    public Optional<Livro> consultarLivro(int idLivro) {
        return acervo.consultarLivro(idLivro);
    }

    @Override
    public boolean excluirLivro(int idLivro) {
        Optional<Livro> opt = acervo.consultarLivro(idLivro);
        if (opt.isEmpty()) return false;
        Livro livro = opt.get();
        if (!livro.excluir()) return false;
        acervo.salvar(livro);
        return true;
    }
}
