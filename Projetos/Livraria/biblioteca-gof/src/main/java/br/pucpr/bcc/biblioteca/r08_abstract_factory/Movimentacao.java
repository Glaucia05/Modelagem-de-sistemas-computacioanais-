package br.pucpr.bcc.biblioteca.r08_abstract_factory;

import br.pucpr.bcc.biblioteca.dominio.*;
import java.util.List;
import java.util.Optional;

public interface Movimentacao {
    default UnidadeTrabalho unidadeTrabalho() { return null; }
    void inserirReserva(Reserva reserva);
    Optional<Reserva> consultarReserva(int idLivro, int idEstudante);
    void removerReserva(int idLivro, int idEstudante);
    void inserirEmprestimo(Emprestimo emprestimo);
    Optional<Emprestimo> consultarEmprestimo(int idLivro, int idEstudante);
    void inserirDevolucao(Devolucao devolucao);
    Optional<Devolucao> consultarDevolucao(int idLivro, int idEstudante);
    void salvarBoleto(BoletoMulta boleto);
    List<BoletoMulta> listarBoletos();
}
