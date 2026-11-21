package br.pucpr.bcc.biblioteca.cadastro;

import br.pucpr.bcc.biblioteca.dominio.Estudante;
import br.pucpr.bcc.biblioteca.persistencia.GruposDoSistema;
import java.util.Optional;

public class CadastroEstudanteTemplate extends RotinaCadastroTemplate<Estudante> {
    private final GruposDoSistema repo;
    private final int idEstudante;
    private final String cpf;
    private final String matricula;

    public CadastroEstudanteTemplate(GruposDoSistema repo, int idEstudante, String cpf, String matricula) {
        this.repo = repo;
        this.idEstudante = idEstudante;
        this.cpf = cpf;
        this.matricula = matricula;
    }

    @Override protected Optional<Estudante> buscarExistente() { return repo.consultarEstudante(idEstudante); }

    @Override
    protected void validar() {
        if (idEstudante <= 0 || cpf == null || cpf.isBlank() || matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("Dados inválidos de Estudante");
        }
    }

    @Override
    protected void verificarPreRequisitos() {
        if (repo.consultarPessoa(cpf).isEmpty()) {
            throw new IllegalStateException("CPF não cadastrado em Pessoa/RH: " + cpf);
        }
    }

    @Override protected Estudante criar() { return new Estudante(idEstudante, cpf, matricula); }
    @Override protected void armazenar(Estudante entidade) { repo.inserirEstudante(entidade); }
}
