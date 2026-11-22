package br.pucpr.bcc.biblioteca.r08_abstract_factory;

import br.pucpr.bcc.biblioteca.dominio.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MemoriaPersistenciaFactory implements PersistenciaFactory {
    private final MemoriaUnidadeTrabalho uow = new MemoriaUnidadeTrabalho();
    private final MemoriaAcervoDeLivros acervo = new MemoriaAcervoDeLivros(uow);
    private final MemoriaMovimentacao movimentacao = new MemoriaMovimentacao(uow);
    private final MemoriaRelacionamento relacionamento = new MemoriaRelacionamento(uow);

    public MemoriaPersistenciaFactory() {
        movimentacao.validarVinculos = (livro, estudante) -> {
            if (!acervo.dados.containsKey(livro) || !relacionamento.estudantes.containsKey(estudante)) {
                throw new IllegalStateException("Livro ou estudante não cadastrado");
            }
        };
        relacionamento.estudanteReferenciado = id -> movimentacao.reservas.stream().anyMatch(r -> r.getIdEstudante() == id)
                || movimentacao.emprestimos.stream().anyMatch(e -> e.getIdEstudante() == id)
                || movimentacao.devolucoes.stream().anyMatch(d -> d.getIdEstudante() == id)
                || movimentacao.boletos.stream().anyMatch(b -> b.idEstudante() == id);
        acervo.livroReferenciado = id -> movimentacao.reservas.stream().anyMatch(r -> r.getIdLivro() == id)
                || movimentacao.emprestimos.stream().anyMatch(e -> e.getIdLivro() == id)
                || movimentacao.devolucoes.stream().anyMatch(d -> d.getIdLivro() == id)
                || movimentacao.boletos.stream().anyMatch(b -> b.idLivro() == id);
    }

    @Override public String familia() { return "MEMORIA"; }
    @Override public UnidadeTrabalho unidadeTrabalho() { return uow; }
    @Override public AcervoDeLivros acervoDeLivros() { return acervo; }
    @Override public Movimentacao movimentacao() { return movimentacao; }
    @Override public Relacionamento relacionamento() { return relacionamento; }

    public static class MemoriaUnidadeTrabalho implements UnidadeTrabalho {
        private boolean ativa;
        private final List<java.util.function.Supplier<Runnable>> participantes = new ArrayList<>();
        private List<Runnable> restauracoes = List.of();
        private void registrar(java.util.function.Supplier<Runnable> snapshot) { participantes.add(snapshot); }
        @Override public synchronized void iniciar() {
            if (ativa) throw new IllegalStateException("Transação já ativa");
            restauracoes = participantes.stream().map(java.util.function.Supplier::get).toList();
            ativa = true;
        }
        @Override public synchronized void commit() { restauracoes = List.of(); ativa = false; }
        @Override public synchronized void rollback() {
            if (ativa) restauracoes.forEach(Runnable::run);
            restauracoes = List.of(); ativa = false;
        }
        @Override public synchronized boolean isAtiva() { return ativa; }
    }

    private static Livro copiar(Livro l) {
        Livro c = new Livro(l.getIdLivro(), l.getTitulo());
        c.reconstituirEstadoPorNome(l.getEstadoNome()); c.setNumRenovacoes(l.getNumRenovacoes());
        c.setIdReservadoParaEstudante(l.getIdReservadoParaEstudante());
        if (l.isExcluido()) c.marcarExcluido();
        return c;
    }
    private static Reserva copiar(Reserva r) {
        Reserva c = new Reserva(r.getIdLivro(), r.getIdEstudante(), r.getData());
        if (!r.isAtiva()) c.cancelar();
        return c;
    }
    private static Emprestimo copiar(Emprestimo e) {
        Emprestimo c = new Emprestimo(e.getIdLivro(), e.getIdEstudante(), e.getDataInicio(), e.getDataVencimento());
        if (e.getIdEmprestimo() > 0) c.definirIdPersistido(e.getIdEmprestimo());
        c.reconstituirSituacao(e.getEstado(), e.isDevolvido());
        return c;
    }

    public static class MemoriaAcervoDeLivros implements AcervoDeLivros {
        private java.util.function.IntPredicate livroReferenciado = id -> false;
        private final MemoriaUnidadeTrabalho uow;
        public MemoriaAcervoDeLivros() { this(new MemoriaUnidadeTrabalho()); }
        public MemoriaAcervoDeLivros(MemoriaUnidadeTrabalho uow) {
            this.uow = uow;
            uow.registrar(() -> { Map<Integer, Livro> antes = new HashMap<>(dados);
                return () -> { dados.clear(); dados.putAll(antes); }; });
        }
        @Override public UnidadeTrabalho unidadeTrabalho() {
            synchronized (uow) {
                return uow;
            }
        }

        private final Map<Integer, Livro> dados = new ConcurrentHashMap<>();
        @Override public void salvar(Livro livro) {
            synchronized (uow) {
                dados.put(livro.getIdLivro(), copiar(livro));
            }
        }
        @Override public Optional<Livro> consultarLivro(int idLivro) {
            synchronized (uow) {
                return Optional.ofNullable(dados.get(idLivro)).map(MemoriaPersistenciaFactory::copiar);
            }
        }
        @Override public List<Livro> listarTodos() {
            synchronized (uow) {
                return dados.values().stream().map(MemoriaPersistenciaFactory::copiar).toList();
            }
        }
        @Override public void excluirLivro(int idLivro) {
            synchronized (uow) {
                if (livroReferenciado.test(idLivro)) throw new IllegalStateException("Livro possui movimentações");
                dados.remove(idLivro);
            }
        }
    }

    public static class MemoriaMovimentacao implements Movimentacao {
        private java.util.function.BiConsumer<Integer, Integer> validarVinculos = (livro, estudante) -> {};
        private long sequenciaEmprestimo;
        private final MemoriaUnidadeTrabalho uow;
        public MemoriaMovimentacao() { this(new MemoriaUnidadeTrabalho()); }
        public MemoriaMovimentacao(MemoriaUnidadeTrabalho uow) {
            this.uow = uow;
            uow.registrar(() -> { var r = new ArrayList<>(reservas); var e = new ArrayList<>(emprestimos);
                var d = new ArrayList<>(devolucoes); var b = new ArrayList<>(boletos);
                return () -> { reservas.clear(); reservas.addAll(r); emprestimos.clear(); emprestimos.addAll(e);
                    devolucoes.clear(); devolucoes.addAll(d); boletos.clear(); boletos.addAll(b); }; });
        }
        @Override public UnidadeTrabalho unidadeTrabalho() {
            synchronized (uow) {
                return uow;
            }
        }

        private final List<Reserva> reservas = new ArrayList<>();
        private final List<Emprestimo> emprestimos = new ArrayList<>();
        private final List<Devolucao> devolucoes = new ArrayList<>();
        private final List<BoletoMulta> boletos = new ArrayList<>();

        @Override
        public void inserirReserva(Reserva reserva) {
            synchronized (uow) {
                validarVinculos.accept(reserva.getIdLivro(), reserva.getIdEstudante());

                reservas.removeIf(r -> r.getIdLivro() == reserva.getIdLivro() && r.getIdEstudante() == reserva.getIdEstudante());
                reservas.add(copiar(reserva));

            }
        }

        @Override
        public Optional<Reserva> consultarReserva(int idLivro, int idEstudante) {
            synchronized (uow) {

                return reservas.stream()
                .filter(Reserva::isAtiva)
                .filter(r -> r.getIdLivro() == idLivro && r.getIdEstudante() == idEstudante)
                .findFirst().map(MemoriaPersistenciaFactory::copiar);

            }
        }

        @Override
        public void removerReserva(int idLivro, int idEstudante) {
            synchronized (uow) {

                reservas.removeIf(r -> r.getIdLivro() == idLivro && r.getIdEstudante() == idEstudante);

            }
        }

        @Override
        public void inserirEmprestimo(Emprestimo emprestimo) {
            synchronized (uow) {
                validarVinculos.accept(emprestimo.getIdLivro(), emprestimo.getIdEstudante());

                if (emprestimo.getIdEmprestimo() == 0) emprestimo.definirIdPersistido(++sequenciaEmprestimo);
                emprestimos.removeIf(e -> e.getIdEmprestimo() == emprestimo.getIdEmprestimo());
                emprestimos.add(copiar(emprestimo));

            }
        }

        @Override
        public Optional<Emprestimo> consultarEmprestimo(int idLivro, int idEstudante) {
            synchronized (uow) {

                return emprestimos.stream()
                .filter(e -> !e.isDevolvido())
                .filter(e -> e.getIdLivro() == idLivro && e.getIdEstudante() == idEstudante)
                .findFirst().map(MemoriaPersistenciaFactory::copiar);

            }
        }

        @Override
        public void inserirDevolucao(Devolucao devolucao) {
            synchronized (uow) {
                validarVinculos.accept(devolucao.getIdLivro(), devolucao.getIdEstudante());
                devolucoes.add(devolucao);
            }
        }

        @Override
        public Optional<Devolucao> consultarDevolucao(int idLivro, int idEstudante) {
            synchronized (uow) {

                return devolucoes.stream()
                .filter(d -> d.getIdLivro() == idLivro && d.getIdEstudante() == idEstudante)
                .reduce((first, second) -> second);

            }
        }

        @Override
        public void salvarBoleto(BoletoMulta boleto) {
            synchronized (uow) {
                validarVinculos.accept(boleto.idLivro(), boleto.idEstudante());
                boletos.removeIf(b -> b.idCobranca().equals(boleto.idCobranca())); boletos.add(boleto);
            }
        }

        @Override
        public List<BoletoMulta> listarBoletos() {
            synchronized (uow) {
                return new ArrayList<>(boletos);
            }
        }
    }

    public static class MemoriaRelacionamento implements Relacionamento {
        private java.util.function.IntPredicate estudanteReferenciado = id -> false;
        private final MemoriaUnidadeTrabalho uow;
        public MemoriaRelacionamento() { this(new MemoriaUnidadeTrabalho()); }
        public MemoriaRelacionamento(MemoriaUnidadeTrabalho uow) {
            this.uow = uow;
            uow.registrar(() -> { var p = new HashMap<>(pessoas); var e = new HashMap<>(estudantes); var a = new HashMap<>(atendentes);
                return () -> { pessoas.clear(); pessoas.putAll(p); estudantes.clear(); estudantes.putAll(e);
                    atendentes.clear(); atendentes.putAll(a); }; });
        }
        @Override public UnidadeTrabalho unidadeTrabalho() {
            synchronized (uow) {
                return uow;
            }
        }

        private final Map<String, Pessoa> pessoas = new ConcurrentHashMap<>();
        private final Map<Integer, Estudante> estudantes = new ConcurrentHashMap<>();
        private final Map<Integer, Atendente> atendentes = new ConcurrentHashMap<>();

        @Override public void inserirPessoa(Pessoa pessoa) {
            synchronized (uow) {
                pessoas.put(pessoa.cpf(), pessoa);
            }
        }
        @Override public Optional<Pessoa> consultarPessoa(String cpf) {
            synchronized (uow) {
                return Optional.ofNullable(pessoas.get(cpf));
            }
        }
        @Override public void excluirPessoa(String cpf) {
            synchronized (uow) {
                if (estudantes.values().stream().anyMatch(e -> e.cpf().equals(cpf))
                        || atendentes.values().stream().anyMatch(a -> a.cpf().equals(cpf)))
                    throw new IllegalStateException("Pessoa possui estudante ou atendente vinculado");
                pessoas.remove(cpf);
            }
        }
        @Override public void inserirEstudante(Estudante estudante) {
            synchronized (uow) {
                if (!pessoas.containsKey(estudante.cpf())) throw new IllegalStateException("CPF não cadastrado");
                estudantes.put(estudante.idEstudante(), estudante);
            }
        }
        @Override public Optional<Estudante> consultarEstudante(int idEstudante) {
            synchronized (uow) {
                return Optional.ofNullable(estudantes.get(idEstudante));
            }
        }
        @Override public void excluirEstudante(int idEstudante) {
            synchronized (uow) {
                if (estudanteReferenciado.test(idEstudante)) throw new IllegalStateException("Estudante possui movimentações");
                estudantes.remove(idEstudante);
            }
        }
        @Override public void inserirAtendente(Atendente atendente) {
            synchronized (uow) {
                if (!pessoas.containsKey(atendente.cpf())) throw new IllegalStateException("CPF não cadastrado");
                atendentes.put(atendente.idAtendente(), atendente);
            }
        }
        @Override public Optional<Atendente> consultarAtendente(int idAtendente) {
            synchronized (uow) {
                return Optional.ofNullable(atendentes.get(idAtendente));
            }
        }
        @Override public void excluirAtendente(int idAtendente) {
            synchronized (uow) {
                atendentes.remove(idAtendente);
            }
        }
    }
}
