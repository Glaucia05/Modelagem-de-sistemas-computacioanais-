package br.pucpr.bcc.biblioteca.tratadores;

import br.pucpr.bcc.biblioteca.dominio.*;
import br.pucpr.bcc.biblioteca.interfaces.*;
import br.pucpr.bcc.biblioteca.cadastro.*;
import br.pucpr.bcc.biblioteca.persistencia.*;

import java.util.Optional;

public class TratadorDeCadastro implements iLivro, iPessoa, iEstudante, iAtendente {
    private final AcervoDeLivros acervo;
    private final GruposDoSistema grupos;

    public TratadorDeCadastro(AcervoDeLivros acervo, GruposDoSistema grupos) {
        this.acervo = acervo;
        this.grupos = grupos;
    }

    @Override
    public Pessoa inserirPessoa(String nome, String sobrenome, String cpf) {
        return new CadastroPessoaTemplate(grupos, nome, sobrenome, cpf).incluir().entidade();
    }

    @Override
    public Pessoa atualizarPessoa(String cpf, String novoNome, String novoSobrenome) {
        Pessoa atualizada = new Pessoa(novoNome, novoSobrenome, cpf);
        grupos.inserirPessoa(atualizada);
        return atualizada;
    }

    @Override
    public Optional<Pessoa> consultarPessoa(String cpf) {
        return grupos.consultarPessoa(cpf);
    }

    @Override
    public boolean excluirPessoa(String cpf) {
        if (grupos.consultarPessoa(cpf).isEmpty()) return false;
        grupos.excluirPessoa(cpf);
        return true;
    }

    @Override
    public Estudante inserirEstudante(String cpf, int idEstudante, String matricula) {
        return new CadastroEstudanteTemplate(grupos, idEstudante, cpf, matricula).incluir().entidade();
    }

    public RotinaCadastroTemplate.ResultadoCadastro<Estudante> inserirEstudanteComResultado(int idEstudante, String cpf, String matricula) {
        return new CadastroEstudanteTemplate(grupos, idEstudante, cpf, matricula).incluir();
    }

    @Override
    public Estudante atualizarEstudante(int idEstudante, String cpf, String novaMatricula) {
        if (grupos.consultarPessoa(cpf).isEmpty()) {
            throw new IllegalStateException("CPF não cadastrado em Pessoa/RH: " + cpf);
        }
        Estudante atualizado = new Estudante(idEstudante, cpf, novaMatricula);
        grupos.inserirEstudante(atualizado);
        return atualizado;
    }

    @Override
    public Optional<Estudante> consultarEstudante(int idEstudante) {
        return grupos.consultarEstudante(idEstudante);
    }

    @Override
    public boolean excluirEstudante(int idEstudante) {
        if (grupos.consultarEstudante(idEstudante).isEmpty()) return false;
        grupos.excluirEstudante(idEstudante);
        return true;
    }

    @Override
    public Atendente inserirAtendente(String cpf, int idAtendente) {
        return new CadastroAtendenteTemplate(grupos, idAtendente, cpf).incluir().entidade();
    }

    @Override
    public Atendente atualizarAtendente(int idAtendente, String novoCpf) {
        if (grupos.consultarPessoa(novoCpf).isEmpty()) {
            throw new IllegalStateException("CPF não cadastrado para Atendente: " + novoCpf);
        }
        Atendente atualizado = new Atendente(idAtendente, novoCpf);
        grupos.inserirAtendente(atualizado);
        return atualizado;
    }

    @Override
    public Optional<Atendente> consultarAtendente(int idAtendente) {
        return grupos.consultarAtendente(idAtendente);
    }

    @Override
    public boolean excluirAtendente(int idAtendente) {
        if (grupos.consultarAtendente(idAtendente).isEmpty()) return false;
        grupos.excluirAtendente(idAtendente);
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
        acervo.inserirLivro(livro);
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
        acervo.inserirLivro(livro);
        return true;
    }
}
