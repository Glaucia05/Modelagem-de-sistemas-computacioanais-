package br.pucpr.bcc.biblioteca.cadastro;

import java.util.Optional;

public abstract class RotinaCadastroTemplate<T> {

    public record ResultadoCadastro<T>(boolean sucesso, boolean jaExistia, T entidade, String mensagem) {}

    public final ResultadoCadastro<T> incluir() {
        Optional<T> existente = buscarExistente();
        if (existente.isPresent()) {
            T ent = existente.get();
            apresentar(ent);
            return new ResultadoCadastro<>(true, true, ent, "Cadastro existente retornado");
        }

        validar();
        verificarPreRequisitos();

        T novaEntidade = criar();
        armazenar(novaEntidade);
        apresentar(novaEntidade);
        return new ResultadoCadastro<>(true, false, novaEntidade, "Cadastro realizado");
    }

    protected abstract Optional<T> buscarExistente();
    protected abstract void validar();
    protected abstract void verificarPreRequisitos();
    protected abstract T criar();
    protected abstract void armazenar(T entidade);

    protected void apresentar(T entidade) {
    }
}
