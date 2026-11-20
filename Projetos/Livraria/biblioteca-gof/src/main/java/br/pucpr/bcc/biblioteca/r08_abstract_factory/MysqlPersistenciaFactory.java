package br.pucpr.bcc.biblioteca.r08_abstract_factory;

import br.pucpr.bcc.biblioteca.dominio.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Família relacional MySQL (perfil de produção).
 * Todos os repositórios compartilham a mesma MysqlUnidadeTrabalho (mesma Connection JDBC).
 * O banco Oracle permanece apenas como réplica física e não recebe escritas da aplicação.
 */
public class MysqlPersistenciaFactory implements PersistenciaFactory {
    private final MysqlUnidadeTrabalho uow;
    private final MysqlRepositorioLivro repoLivro;
    private final MysqlRepositorioMovimentacao repoMov;
    private final MysqlRepositorioRelacionamento repoRel;

    public MysqlPersistenciaFactory() {
        this(
                System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/biblioteca_ib2b"),
                System.getenv().getOrDefault("DB_USER", "root"),
                System.getenv().getOrDefault("DB_PASS", "")
        );
    }

    public MysqlPersistenciaFactory(String url, String user, String pass) {
        this.uow = new MysqlUnidadeTrabalho(url, user, pass);
        this.repoLivro = new MysqlRepositorioLivro(uow);
        this.repoMov = new MysqlRepositorioMovimentacao(uow);
        this.repoRel = new MysqlRepositorioRelacionamento(uow);
    }

    @Override public String familia() { return "MYSQL_PROD"; }
    @Override public UnidadeTrabalho unidadeTrabalho() { return uow; }
    @Override public RepositorioLivro repositorioLivro() { return repoLivro; }
    @Override public RepositorioMovimentacao repositorioMovimentacao() { return repoMov; }
    @Override public RepositorioRelacionamento repositorioRelacionamento() { return repoRel; }

    public static class MysqlUnidadeTrabalho implements UnidadeTrabalho {
        private final String url;
        private final String user;
        private final String pass;
        private Connection connection;
        private boolean ativa = false;

        public MysqlUnidadeTrabalho(String url, String user, String pass) {
            this.url = url;
            this.user = user;
            this.pass = pass;
        }

        public Connection getConnection() throws SQLException {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, pass);
            }
            return connection;
        }

        @Override
        public void iniciar() {
            ativa = true;
            try {
                getConnection().setAutoCommit(false);
            } catch (SQLException e) {
                throw new IllegalStateException("Erro ao iniciar transação MySQL: " + e.getMessage(), e);
            }
        }

        @Override
        public void commit() {
            ativa = false;
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.commit();
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Erro no commit MySQL: " + e.getMessage(), e);
            }
        }

        @Override
        public void rollback() {
            ativa = false;
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Erro no rollback MySQL: " + e.getMessage(), e);
            }
        }

        @Override
        public boolean isAtiva() { return ativa; }
    }

    public static class MysqlRepositorioLivro implements RepositorioLivro {
        private final MysqlUnidadeTrabalho uow;
        public MysqlRepositorioLivro(MysqlUnidadeTrabalho uow) { this.uow = uow; }
        public boolean usaMesmaTransacao(UnidadeTrabalho outra) { return this.uow == outra; }

        @Override
        public void salvar(Livro livro) {
            String sql = """
                INSERT INTO livros (id_livro, titulo, estado, id_reservado_estudante, num_renovacoes, excluido)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE titulo = VALUES(titulo), estado = VALUES(estado),
                id_reservado_estudante = VALUES(id_reservado_estudante), num_renovacoes = VALUES(num_renovacoes),
                excluido = VALUES(excluido)
                """;
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                ps.setInt(1, livro.getIdLivro());
                ps.setString(2, livro.getTitulo());
                ps.setString(3, livro.getEstadoNome());
                if (livro.getIdReservadoParaEstudante() != null) {
                    ps.setInt(4, livro.getIdReservadoParaEstudante());
                } else {
                    ps.setNull(4, Types.INTEGER);
                }
                ps.setInt(5, livro.getNumRenovacoes());
                ps.setBoolean(6, livro.isExcluido());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao salvar Livro: " + e.getMessage(), e);
            }
        }

        @Override
        public Optional<Livro> buscarPorId(int idLivro) {
            String sql = "SELECT id_livro, titulo, estado, id_reservado_estudante, num_renovacoes, excluido FROM livros WHERE id_livro = ?";
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                ps.setInt(1, idLivro);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    Livro l = new Livro(rs.getInt("id_livro"), rs.getString("titulo"));
                    l.reconstituirEstadoPorNome(rs.getString("estado"));
                    int resEst = rs.getInt("id_reservado_estudante");
                    if (!rs.wasNull()) l.setIdReservadoParaEstudante(resEst);
                    l.setNumRenovacoes(rs.getInt("num_renovacoes"));
                    if (rs.getBoolean("excluido")) l.marcarExcluido();
                    return Optional.of(l);
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao buscar Livro: " + e.getMessage(), e);
            }
        }

        @Override
        public List<Livro> listarTodos() {
            String sql = "SELECT id_livro, titulo, estado, id_reservado_estudante, num_renovacoes, excluido FROM livros";
            List<Livro> lista = new ArrayList<>();
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Livro l = new Livro(rs.getInt("id_livro"), rs.getString("titulo"));
                    l.reconstituirEstadoPorNome(rs.getString("estado"));
                    l.setNumRenovacoes(rs.getInt("num_renovacoes"));
                    lista.add(l);
                }
                return lista;
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao listar Livros: " + e.getMessage(), e);
            }
        }

        @Override
        public void remover(int idLivro) {
            try (PreparedStatement ps = uow.getConnection().prepareStatement("DELETE FROM livros WHERE id_livro = ?")) {
                ps.setInt(1, idLivro);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao remover Livro: " + e.getMessage(), e);
            }
        }
    }

    public static class MysqlRepositorioMovimentacao implements RepositorioMovimentacao {
        private final MysqlUnidadeTrabalho uow;
        public MysqlRepositorioMovimentacao(MysqlUnidadeTrabalho uow) { this.uow = uow; }
        public boolean usaMesmaTransacao(UnidadeTrabalho outra) { return this.uow == outra; }

        @Override
        public void salvarReserva(Reserva r) {
            String sql = "REPLACE INTO reservas (id_livro, id_estudante, data_reserva, ativa) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                ps.setInt(1, r.getIdLivro());
                ps.setInt(2, r.getIdEstudante());
                ps.setDate(3, Date.valueOf(r.getData()));
                ps.setBoolean(4, r.isAtiva());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao salvar Reserva: " + e.getMessage(), e);
            }
        }

        @Override
        public Optional<Reserva> buscarReservaAtiva(int idLivro, int idEstudante) {
            String sql = "SELECT data_reserva FROM reservas WHERE id_livro = ? AND id_estudante = ? AND ativa = TRUE";
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                ps.setInt(1, idLivro);
                ps.setInt(2, idEstudante);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    return Optional.of(new Reserva(idLivro, idEstudante, rs.getDate("data_reserva").toLocalDate()));
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao buscar Reserva: " + e.getMessage(), e);
            }
        }

        @Override
        public void removerReserva(int idLivro, int idEstudante) {
            String sql = "DELETE FROM reservas WHERE id_livro = ? AND id_estudante = ?";
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                ps.setInt(1, idLivro);
                ps.setInt(2, idEstudante);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao remover Reserva: " + e.getMessage(), e);
            }
        }

        @Override
        public void salvarEmprestimo(Emprestimo emp) {
            String sql = "INSERT INTO emprestimos (id_livro, id_estudante, data_inicio, data_vencimento, estado, devolvido) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                ps.setInt(1, emp.getIdLivro());
                ps.setInt(2, emp.getIdEstudante());
                ps.setDate(3, Date.valueOf(emp.getDataInicio()));
                ps.setDate(4, Date.valueOf(emp.getDataVencimento()));
                ps.setString(5, emp.getEstado());
                ps.setBoolean(6, emp.isDevolvido());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao salvar Empréstimo: " + e.getMessage(), e);
            }
        }

        @Override
        public Optional<Emprestimo> buscarEmprestimoAtivo(int idLivro, int idEstudante) {
            String sql = "SELECT data_inicio, data_vencimento FROM emprestimos WHERE id_livro = ? AND id_estudante = ? AND devolvido = FALSE ORDER BY id_emprestimo DESC LIMIT 1";
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                ps.setInt(1, idLivro);
                ps.setInt(2, idEstudante);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    return Optional.of(new Emprestimo(idLivro, idEstudante,
                            rs.getDate("data_inicio").toLocalDate(),
                            rs.getDate("data_vencimento").toLocalDate()));
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao buscar Empréstimo: " + e.getMessage(), e);
            }
        }

        @Override
        public void salvarDevolucao(Devolucao d) {
            String sql = "INSERT INTO devolucoes (id_livro, id_estudante, data_devolucao, data_vencimento) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                ps.setInt(1, d.getIdLivro());
                ps.setInt(2, d.getIdEstudante());
                ps.setDate(3, Date.valueOf(d.getDataDevolucao()));
                ps.setDate(4, Date.valueOf(d.getDataVencimento()));
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao salvar Devolução: " + e.getMessage(), e);
            }
        }

        @Override
        public Optional<Devolucao> buscarDevolucao(int idLivro, int idEstudante) {
            String sql = "SELECT data_devolucao, data_vencimento FROM devolucoes WHERE id_livro = ? AND id_estudante = ? ORDER BY id_devolucao DESC LIMIT 1";
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                ps.setInt(1, idLivro);
                ps.setInt(2, idEstudante);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    return Optional.of(new Devolucao(idLivro, idEstudante,
                            rs.getDate("data_devolucao").toLocalDate(),
                            rs.getDate("data_vencimento").toLocalDate()));
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao buscar Devolução: " + e.getMessage(), e);
            }
        }

        @Override
        public void salvarBoleto(BoletoMulta b) {
            String sql = "REPLACE INTO boletos_multa (id_cobranca, id_livro, id_estudante, valor, politica_aplicada, situacao) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                ps.setString(1, b.idCobranca());
                ps.setInt(2, b.idLivro());
                ps.setInt(3, b.idEstudante());
                ps.setBigDecimal(4, b.valor());
                ps.setString(5, b.politicaAplicada());
                ps.setString(6, b.situacao());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao salvar Boleto: " + e.getMessage(), e);
            }
        }

        @Override
        public List<BoletoMulta> listarBoletos() {
            String sql = "SELECT id_cobranca, id_livro, id_estudante, valor, politica_aplicada, situacao FROM boletos_multa";
            List<BoletoMulta> lista = new ArrayList<>();
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new BoletoMulta(rs.getString("id_cobranca"), rs.getInt("id_livro"),
                            rs.getInt("id_estudante"), rs.getBigDecimal("valor"),
                            rs.getString("politica_aplicada"), rs.getString("situacao")));
                }
                return lista;
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao listar Boletos: " + e.getMessage(), e);
            }
        }
    }

    public static class MysqlRepositorioRelacionamento implements RepositorioRelacionamento {
        private final MysqlUnidadeTrabalho uow;
        public MysqlRepositorioRelacionamento(MysqlUnidadeTrabalho uow) { this.uow = uow; }
        public boolean usaMesmaTransacao(UnidadeTrabalho outra) { return this.uow == outra; }

        @Override
        public void salvarPessoa(Pessoa p) {
            String sql = "REPLACE INTO pessoas (cpf, nome, sobrenome) VALUES (?, ?, ?)";
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                ps.setString(1, p.cpf());
                ps.setString(2, p.nome());
                ps.setString(3, p.sobrenome());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao salvar Pessoa: " + e.getMessage(), e);
            }
        }

        @Override
        public Optional<Pessoa> buscarPessoaPorCpf(String cpf) {
            try (PreparedStatement ps = uow.getConnection().prepareStatement("SELECT cpf, nome, sobrenome FROM pessoas WHERE cpf = ?")) {
                ps.setString(1, cpf);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    return Optional.of(new Pessoa(rs.getString("nome"), rs.getString("sobrenome"), rs.getString("cpf")));
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao buscar Pessoa: " + e.getMessage(), e);
            }
        }

        @Override
        public void removerPessoa(String cpf) {
            try (PreparedStatement ps = uow.getConnection().prepareStatement("DELETE FROM pessoas WHERE cpf = ?")) {
                ps.setString(1, cpf);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao remover Pessoa: " + e.getMessage(), e);
            }
        }

        @Override
        public void salvarEstudante(Estudante est) {
            String sql = "REPLACE INTO estudantes (id_estudante, cpf, matricula) VALUES (?, ?, ?)";
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                ps.setInt(1, est.idEstudante());
                ps.setString(2, est.cpf());
                ps.setString(3, est.matricula());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao salvar Estudante: " + e.getMessage(), e);
            }
        }

        @Override
        public Optional<Estudante> buscarEstudantePorId(int idEstudante) {
            try (PreparedStatement ps = uow.getConnection().prepareStatement("SELECT id_estudante, cpf, matricula FROM estudantes WHERE id_estudante = ?")) {
                ps.setInt(1, idEstudante);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    return Optional.of(new Estudante(rs.getInt("id_estudante"), rs.getString("cpf"), rs.getString("matricula")));
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao buscar Estudante: " + e.getMessage(), e);
            }
        }

        @Override
        public void removerEstudante(int idEstudante) {
            try (PreparedStatement ps = uow.getConnection().prepareStatement("DELETE FROM estudantes WHERE id_estudante = ?")) {
                ps.setInt(1, idEstudante);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao remover Estudante: " + e.getMessage(), e);
            }
        }

        @Override
        public void salvarAtendente(Atendente at) {
            String sql = "REPLACE INTO atendentes (id_atendente, cpf) VALUES (?, ?)";
            try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                ps.setInt(1, at.idAtendente());
                ps.setString(2, at.cpf());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao salvar Atendente: " + e.getMessage(), e);
            }
        }

        @Override
        public Optional<Atendente> buscarAtendentePorId(int idAtendente) {
            try (PreparedStatement ps = uow.getConnection().prepareStatement("SELECT id_atendente, cpf FROM atendentes WHERE id_atendente = ?")) {
                ps.setInt(1, idAtendente);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    return Optional.of(new Atendente(rs.getInt("id_atendente"), rs.getString("cpf")));
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao buscar Atendente: " + e.getMessage(), e);
            }
        }

        @Override
        public void removerAtendente(int idAtendente) {
            try (PreparedStatement ps = uow.getConnection().prepareStatement("DELETE FROM atendentes WHERE id_atendente = ?")) {
                ps.setInt(1, idAtendente);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new IllegalStateException("Erro JDBC ao remover Atendente: " + e.getMessage(), e);
            }
        }
    }
}
