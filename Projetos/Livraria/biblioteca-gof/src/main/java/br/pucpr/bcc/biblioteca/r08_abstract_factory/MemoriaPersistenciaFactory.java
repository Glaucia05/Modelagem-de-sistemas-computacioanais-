package br.pucpr.bcc.biblioteca.r08_abstract_factory;

import br.pucpr.bcc.biblioteca.dominio.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MemoriaPersistenciaFactory implements PersistenciaFactory {
    private final MemoriaUnidadeTrabalho uow = new MemoriaUnidadeTrabalho();
    private final MemoriaRepositorioLivro repoLivro = new MemoriaRepositorioLivro();
    private final MemoriaRepositorioMovimentacao repoMov = new MemoriaRepositorioMovimentacao();
    private final MemoriaRepositorioRelacionamento repoRel = new MemoriaRepositorioRelacionamento();

    @Override public String familia() { return "MEMORIA"; }
    @Override public UnidadeTrabalho unidadeTrabalho() { return uow; }
    @Override public RepositorioLivro repositorioLivro() { return repoLivro; }
    @Override public RepositorioMovimentacao repositorioMovimentacao() { return repoMov; }
    @Override public RepositorioRelacionamento repositorioRelacionamento() { return repoRel; }

    public static class MemoriaUnidadeTrabalho implements UnidadeTrabalho {
        private boolean ativa = false;
        @Override public void iniciar() { ativa = true; }
        @Override public void commit() { ativa = false; }
        @Override public void rollback() { ativa = false; }
        @Override public boolean isAtiva() { return ativa; }
    }

    public static class MemoriaRepositorioLivro implements RepositorioLivro {
        private final Map<Integer, Livro> dados = new ConcurrentHashMap<>();
        @Override public void salvar(Livro livro) { dados.put(livro.getIdLivro(), livro); }
        @Override public Optional<Livro> buscarPorId(int idLivro) { return Optional.ofNullable(dados.get(idLivro)); }
        @Override public List<Livro> listarTodos() { return new ArrayList<>(dados.values()); }
        @Override public void remover(int idLivro) { dados.remove(idLivro); }
    }

    public static class MemoriaRepositorioMovimentacao implements RepositorioMovimentacao {
        private final List<Reserva> reservas = new ArrayList<>();
        private final List<Emprestimo> emprestimos = new ArrayList<>();
        private final List<Devolucao> devolucoes = new ArrayList<>();
        private final List<BoletoMulta> boletos = new ArrayList<>();

        @Override
        public void salvarReserva(Reserva reserva) {
            reservas.removeIf(r -> r.getIdLivro() == reserva.getIdLivro() && r.getIdEstudante() == reserva.getIdEstudante());
            reservas.add(reserva);
        }

        @Override
        public Optional<Reserva> buscarReservaAtiva(int idLivro, int idEstudante) {
            return reservas.stream()
                    .filter(Reserva::isAtiva)
                    .filter(r -> r.getIdLivro() == idLivro && r.getIdEstudante() == idEstudante)
                    .findFirst();
        }

        @Override
        public void removerReserva(int idLivro, int idEstudante) {
            reservas.removeIf(r -> r.getIdLivro() == idLivro && r.getIdEstudante() == idEstudante);
        }

        @Override
        public void salvarEmprestimo(Emprestimo emprestimo) {
            emprestimos.removeIf(e -> e.getIdLivro() == emprestimo.getIdLivro() && !e.isDevolvido());
            emprestimos.add(emprestimo);
        }

        @Override
        public Optional<Emprestimo> buscarEmprestimoAtivo(int idLivro, int idEstudante) {
            return emprestimos.stream()
                    .filter(e -> !e.isDevolvido())
                    .filter(e -> e.getIdLivro() == idLivro && e.getIdEstudante() == idEstudante)
                    .findFirst();
        }

        @Override
        public void salvarDevolucao(Devolucao devolucao) { devolucoes.add(devolucao); }

        @Override
        public Optional<Devolucao> buscarDevolucao(int idLivro, int idEstudante) {
            return devolucoes.stream()
                    .filter(d -> d.getIdLivro() == idLivro && d.getIdEstudante() == idEstudante)
                    .reduce((first, second) -> second);
        }

        @Override
        public void salvarBoleto(BoletoMulta boleto) { boletos.add(boleto); }

        @Override
        public List<BoletoMulta> listarBoletos() { return new ArrayList<>(boletos); }
    }

    public static class MemoriaRepositorioRelacionamento implements RepositorioRelacionamento {
        private final Map<String, Pessoa> pessoas = new ConcurrentHashMap<>();
        private final Map<Integer, Estudante> estudantes = new ConcurrentHashMap<>();
        private final Map<Integer, Atendente> atendentes = new ConcurrentHashMap<>();

        @Override public void salvarPessoa(Pessoa pessoa) { pessoas.put(pessoa.cpf(), pessoa); }
        @Override public Optional<Pessoa> buscarPessoaPorCpf(String cpf) { return Optional.ofNullable(pessoas.get(cpf)); }
        @Override public void removerPessoa(String cpf) { pessoas.remove(cpf); }
        @Override public void salvarEstudante(Estudante estudante) { estudantes.put(estudante.idEstudante(), estudante); }
        @Override public Optional<Estudante> buscarEstudantePorId(int idEstudante) { return Optional.ofNullable(estudantes.get(idEstudante)); }
        @Override public void removerEstudante(int idEstudante) { estudantes.remove(idEstudante); }
        @Override public void salvarAtendente(Atendente atendente) { atendentes.put(atendente.idAtendente(), atendente); }
        @Override public Optional<Atendente> buscarAtendentePorId(int idAtendente) { return Optional.ofNullable(atendentes.get(idAtendente)); }
        @Override public void removerAtendente(int idAtendente) { atendentes.remove(idAtendente); }
    }
}
