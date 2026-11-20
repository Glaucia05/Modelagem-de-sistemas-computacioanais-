package br.pucpr.bcc.biblioteca.r08_abstract_factory;

import br.pucpr.bcc.biblioteca.dominio.*;
import java.util.List;
import java.util.Optional;

public interface RepositorioMovimentacao {
    void salvarReserva(Reserva reserva);
    Optional<Reserva> buscarReservaAtiva(int idLivro, int idEstudante);
    void removerReserva(int idLivro, int idEstudante);
    void salvarEmprestimo(Emprestimo emprestimo);
    Optional<Emprestimo> buscarEmprestimoAtivo(int idLivro, int idEstudante);
    void salvarDevolucao(Devolucao devolucao);
    Optional<Devolucao> buscarDevolucao(int idLivro, int idEstudante);
    void salvarBoleto(BoletoMulta boleto);
    List<BoletoMulta> listarBoletos();
}
