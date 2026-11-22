package br.pucpr.bcc.biblioteca.tratadores;

import br.pucpr.bcc.biblioteca.interfaces.iEmprestimo;
import br.pucpr.bcc.biblioteca.r04_facade.CirculacaoFacade;
import br.pucpr.bcc.biblioteca.r04_facade.ComprovanteOperacao;
import br.pucpr.bcc.biblioteca.r06_command.*;

import java.time.Instant;
import java.time.LocalDate;

public class TratadorDeEmprestimo implements iEmprestimo {
    private final CirculacaoFacade facade;
    private final ExecutorComando executorComando;

    public TratadorDeEmprestimo(CirculacaoFacade facade, ExecutorComando executorComando) {
        this.facade = facade;
        this.executorComando = executorComando;
    }

    @Override
    public ComprovanteOperacao solicitarReserva(String idComando, String autor, int idLivro, int idEstudante, LocalDate data) {
        return executorComando.executar(new ReservarLivroCommand(idComando, autor, Instant.now(), facade, idLivro, idEstudante, data));
    }

    @Override
    public boolean consultarReserva(int idLivro, int idEstudante) {
        return facade.existeReservaAtiva(idLivro, idEstudante);
    }

    @Override
    public ComprovanteOperacao efetivarEmprestimo(int idLivro, int idEstudante, LocalDate dataInicio, int diasPrazo) {
        return facade.efetivarEmprestimo(idLivro, idEstudante, dataInicio, diasPrazo);
    }

    @Override
    public ComprovanteOperacao renovarEmprestimo(String idComando, String autor, int idLivro, int idEstudante) {
        return executorComando.executar(new RenovarEmprestimoCommand(idComando, autor, Instant.now(), facade, idLivro, idEstudante));
    }
}
