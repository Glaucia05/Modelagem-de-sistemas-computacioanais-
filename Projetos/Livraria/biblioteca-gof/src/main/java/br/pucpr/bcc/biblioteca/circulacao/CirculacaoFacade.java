package br.pucpr.bcc.biblioteca.circulacao;

import br.pucpr.bcc.biblioteca.dominio.*;
import br.pucpr.bcc.biblioteca.eventos.*;
import br.pucpr.bcc.biblioteca.persistencia.*;

import java.time.LocalDate;
import java.util.Optional;

public class CirculacaoFacade {
    private final UnidadeTrabalho uow;
    private final AcervoDeLivros acervo;
    private final Movimentacao movimentacao;
    private final GruposDoSistema grupos;
    private final PublicadorDevolucao publicadorDevolucao;

    public CirculacaoFacade(
    UnidadeTrabalho uow,
    AcervoDeLivros acervo,
    Movimentacao movimentacao,
    GruposDoSistema grupos,
    PublicadorDevolucao publicadorDevolucao
    ) {
        for (UnidadeTrabalho origem : new UnidadeTrabalho[]{acervo.unidadeTrabalho(), movimentacao.unidadeTrabalho(), grupos.unidadeTrabalho()}) {
            if (origem != null && origem != uow) throw new IllegalArgumentException("Repositórios devem pertencer à mesma unidade de trabalho");
        }
        this.uow = uow;
        this.acervo = acervo;
        this.movimentacao = movimentacao;
        this.grupos = grupos;
        this.publicadorDevolucao = publicadorDevolucao;
    }

    public ComprovanteOperacao solicitarReserva(int idLivro, int idEstudante, LocalDate data) {
        synchronized (uow) {
            boolean iniciou = false;
            try {
                uow.iniciar();
                iniciou = true;
                java.util.Objects.requireNonNull(data, "Data da reserva obrigatória");
                if (grupos.consultarEstudante(idEstudante).isEmpty()) {
                    uow.rollback();
                    return new ComprovanteOperacao(false, "RESERVA", idLivro, idEstudante, null, null, "Estudante não cadastrado");
                }
                Optional<Livro> optLivro = acervo.consultarLivro(idLivro);
                if (optLivro.isEmpty()) {
                    uow.rollback();
                    return new ComprovanteOperacao(false, "RESERVA", idLivro, idEstudante, null, null, "Livro não cadastrado");
                }
                Livro livro = optLivro.get();
                if (!livro.reservar(idEstudante)) {
                    uow.rollback();
                    return new ComprovanteOperacao(false, "RESERVA", idLivro, idEstudante, livro.getEstadoNome(), null, "Livro indisponível para reserva");
                }
                movimentacao.inserirReserva(new Reserva(idLivro, idEstudante, data));
                acervo.inserirLivro(livro);
                uow.commit();
                return new ComprovanteOperacao(true, "RESERVA", idLivro, idEstudante, livro.getEstadoNome(), null, "Reserva registrada");
            } catch (RuntimeException ex) {
                if (iniciou) desfazer(ex);
                else System.err.println("Não foi possível iniciar a operação: " + ex.getMessage());
                return new ComprovanteOperacao(false, "RESERVA", idLivro, idEstudante, null, null, "Falha na reserva: " + ex.getMessage());
            }
        }
    }

    public boolean existeReservaAtiva(int idLivro, int idEstudante) {
        synchronized (uow) {
            return movimentacao.consultarReserva(idLivro, idEstudante).isPresent();
        }
    }

    public ComprovanteOperacao efetivarEmprestimo(int idLivro, int idEstudante, LocalDate dataInicio, int diasPrazo) {
        synchronized (uow) {
            boolean iniciou = false;
            try {
                uow.iniciar();
                iniciou = true;
                Optional<Livro> optLivro = acervo.consultarLivro(idLivro);
                Optional<Reserva> optReserva = movimentacao.consultarReserva(idLivro, idEstudante);
                if (optReserva.isEmpty() || optLivro.isEmpty()) {
                    uow.rollback();
                    return new ComprovanteOperacao(false, "EMPRESTIMO", idLivro, idEstudante,
                    optLivro.map(Livro::getEstadoNome).orElse(null), null, "Reserva ativa não encontrada para o estudante");
                }

                if (dataInicio == null || diasPrazo <= 0 || dataInicio.isBefore(optReserva.get().getData())) {
                    throw new IllegalArgumentException("Data ou prazo do empréstimo inválido");
                }
                Livro livro = optLivro.get();
                Emprestimo emprestimo = new Emprestimo(idLivro, idEstudante, dataInicio, dataInicio.plusDays(diasPrazo));
                if (!livro.emprestar(idEstudante, emprestimo)) {
                    uow.rollback();
                    return new ComprovanteOperacao(false, "EMPRESTIMO", idLivro, idEstudante, livro.getEstadoNome(), null, "Transição para EMPRESTADO recusada");
                }

                Reserva reserva = optReserva.get();
                reserva.cancelar();
                movimentacao.removerReserva(idLivro, idEstudante);
                movimentacao.inserirEmprestimo(emprestimo);
                acervo.inserirLivro(livro);
                uow.commit();
                return new ComprovanteOperacao(true, "EMPRESTIMO", idLivro, idEstudante, livro.getEstadoNome(),
                emprestimo.getDataVencimento(), "Empréstimo efetivado");
            } catch (RuntimeException ex) {
                if (iniciou) desfazer(ex);
                else System.err.println("Não foi possível iniciar a operação: " + ex.getMessage());
                return new ComprovanteOperacao(false, "EMPRESTIMO", idLivro, idEstudante, null, null, "Falha no empréstimo");
            }
        }
    }

    public ComprovanteOperacao renovarEmprestimo(int idLivro, int idEstudante) {
        synchronized (uow) {
            boolean iniciou = false;
            try {
                uow.iniciar();
                iniciou = true;
                Optional<Livro> optLivro = acervo.consultarLivro(idLivro);
                Optional<Emprestimo> optEmp = movimentacao.consultarEmprestimo(idLivro, idEstudante);
                if (optEmp.isEmpty() || optLivro.isEmpty()) {
                    uow.rollback();
                    return new ComprovanteOperacao(false, "RENOVACAO", idLivro, idEstudante,
                    optLivro.map(Livro::getEstadoNome).orElse(null), null, "Empréstimo ativo não encontrado");
                }

                Emprestimo emprestimo = optEmp.get();
                Livro livro = optLivro.get();
                LocalDate vencimentoAnterior = emprestimo.getDataVencimento();

                if (!livro.renovar(emprestimo)) {
                    uow.rollback();
                    return new ComprovanteOperacao(false, "RENOVACAO", idLivro, idEstudante, livro.getEstadoNome(),
                    vencimentoAnterior, "Renovação recusada");
                }

                movimentacao.inserirEmprestimo(emprestimo);
                acervo.inserirLivro(livro);
                uow.commit();
                return new ComprovanteOperacao(true, "RENOVACAO", idLivro, idEstudante, livro.getEstadoNome(),
                emprestimo.getDataVencimento(), "Empréstimo renovado (+30 dias)");
            } catch (RuntimeException ex) {
                if (iniciou) desfazer(ex);
                else System.err.println("Não foi possível iniciar a operação: " + ex.getMessage());
                return new ComprovanteOperacao(false, "RENOVACAO", idLivro, idEstudante, null, null, "Falha na renovação");
            }
        }
    }

    public ComprovanteOperacao registrarDevolucao(int idLivro, int idEstudante, LocalDate dataDevolucao) {
        synchronized (uow) {
            boolean iniciou = false;
            try {
                uow.iniciar();
                iniciou = true;
                Optional<Livro> optLivro = acervo.consultarLivro(idLivro);
                Optional<Emprestimo> optEmp = movimentacao.consultarEmprestimo(idLivro, idEstudante);
                if (optEmp.isEmpty() || optLivro.isEmpty()) {
                    uow.rollback();
                    return new ComprovanteOperacao(false, "DEVOLUCAO", idLivro, idEstudante,
                    optLivro.map(Livro::getEstadoNome).orElse(null), null, "Empréstimo não encontrado");
                }

                Emprestimo emprestimo = optEmp.get();
                Livro livro = optLivro.get();
                if (dataDevolucao == null || dataDevolucao.isBefore(emprestimo.getDataInicio())) {
                    throw new IllegalArgumentException("Devolução anterior ao empréstimo");
                }
                if (!livro.devolver(emprestimo)) {
                    uow.rollback();
                    return new ComprovanteOperacao(false, "DEVOLUCAO", idLivro, idEstudante, livro.getEstadoNome(),
                    emprestimo.getDataVencimento(), "Devolução recusada");
                }

                emprestimo.marcarDevolvido();
                Devolucao devolucao = new Devolucao(idLivro, idEstudante, dataDevolucao, emprestimo.getDataVencimento());
                movimentacao.inserirDevolucao(devolucao);
                movimentacao.inserirEmprestimo(emprestimo);
                acervo.inserirLivro(livro);
                uow.commit();

                publicadorDevolucao.publicar(new EventoDevolucao(idLivro, idEstudante, emprestimo.getDataVencimento(), dataDevolucao));
                return new ComprovanteOperacao(true, "DEVOLUCAO", idLivro, idEstudante, livro.getEstadoNome(),
                emprestimo.getDataVencimento(), "Devolução registrada");
            } catch (RuntimeException ex) {
                if (iniciou) desfazer(ex);
                else System.err.println("Não foi possível iniciar a operação: " + ex.getMessage());
                return new ComprovanteOperacao(false, "DEVOLUCAO", idLivro, idEstudante, null, null, "Falha na devolução");
            }
        }
    }

    public ComprovanteOperacao inspecionarLivro(int idLivro, boolean bomEstado) {
        synchronized (uow) {
            boolean iniciou = false;
            try {
                uow.iniciar();
                iniciou = true;
                Optional<Livro> optLivro = acervo.consultarLivro(idLivro);
                if (optLivro.isEmpty()) {
                    uow.rollback();
                    return new ComprovanteOperacao(false, "INSPECAO", idLivro, 0, null, null, "Livro não encontrado");
                }
                Livro livro = optLivro.get();
                if (!livro.inspecionar(bomEstado)) {
                    uow.rollback();
                    return new ComprovanteOperacao(false, "INSPECAO", idLivro, 0, livro.getEstadoNome(), null, "Inspeção inválida para estado atual");
                }
                acervo.inserirLivro(livro);
                uow.commit();
                return new ComprovanteOperacao(true, "INSPECAO", idLivro, 0, livro.getEstadoNome(), null, "Inspeção concluída");
            } catch (RuntimeException ex) {
                if (iniciou) desfazer(ex);
                else System.err.println("Não foi possível iniciar a operação: " + ex.getMessage());
                return new ComprovanteOperacao(false, "INSPECAO", idLivro, 0, null, null, "Falha na inspeção");
            }
        }
    }

    public ComprovanteOperacao emitirLaudoIrrecuperavel(int idLivro) {
        synchronized (uow) {
            boolean iniciou = false;
            try {
                uow.iniciar();
                iniciou = true;
                Optional<Livro> optLivro = acervo.consultarLivro(idLivro);
                if (optLivro.isEmpty()) {
                    uow.rollback();
                    return new ComprovanteOperacao(false, "INCINERACAO", idLivro, 0, null, null, "Livro não encontrado");
                }
                Livro livro = optLivro.get();
                if (!livro.incinerar(true)) {
                    uow.rollback();
                    return new ComprovanteOperacao(false, "INCINERACAO", idLivro, 0, livro.getEstadoNome(), null, "Livro não está em REPARANDO");
                }
                acervo.inserirLivro(livro);
                uow.commit();
                return new ComprovanteOperacao(true, "INCINERACAO", idLivro, 0, livro.getEstadoNome(), null, "Laudo irrecuperável aplicado");
            } catch (RuntimeException ex) {
                if (iniciou) desfazer(ex);
                else System.err.println("Não foi possível iniciar a operação: " + ex.getMessage());
                return new ComprovanteOperacao(false, "INCINERACAO", idLivro, 0, null, null, "Falha ao aplicar laudo");
            }
        }
    }
    private void desfazer(RuntimeException original) {
        try { if (uow.isAtiva()) uow.rollback(); }
        catch (RuntimeException falhaRollback) { original.addSuppressed(falhaRollback); }
        System.err.println("Falha na circulação: " + original);
        for (Throwable falha : original.getSuppressed()) System.err.println("Falha adicional: " + falha);
    }
}
