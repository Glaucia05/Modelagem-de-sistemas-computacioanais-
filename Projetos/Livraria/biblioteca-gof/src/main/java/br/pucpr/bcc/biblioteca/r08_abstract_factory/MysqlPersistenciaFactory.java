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
    private final MysqlAcervoDeLivros acervo;
    private final MysqlMovimentacao movimentacao;
    private final MysqlRelacionamento relacionamento;

    public MysqlPersistenciaFactory() {
        this(
        System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/biblioteca_ib2b"),
        System.getenv().getOrDefault("DB_USER", "root"),
        System.getenv().getOrDefault("DB_PASS", "")
        );
    }

    public MysqlPersistenciaFactory(String url, String user, String pass) {
        this.uow = new MysqlUnidadeTrabalho(url, user, pass);
        this.acervo = new MysqlAcervoDeLivros(uow);
        this.movimentacao = new MysqlMovimentacao(uow);
        this.relacionamento = new MysqlRelacionamento(uow);
    }

    @Override public void close() { uow.close(); }
    @Override public String familia() { return "MYSQL_PROD"; }
    @Override public UnidadeTrabalho unidadeTrabalho() { return uow; }
    @Override public AcervoDeLivros acervoDeLivros() { return acervo; }
    @Override public Movimentacao movimentacao() { return movimentacao; }
    @Override public Relacionamento relacionamento() { return relacionamento; }

    public static class MysqlUnidadeTrabalho implements UnidadeTrabalho, AutoCloseable {
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
                if (ativa) throw new SQLException("Conexão perdida durante transação");
                connection = DriverManager.getConnection(url, user, pass);
            }
            return connection;
        }

        @Override
        public synchronized void iniciar() {
            if (ativa) throw new IllegalStateException("Transação já ativa");
            try {
                getConnection().setAutoCommit(false);
                ativa = true;
            } catch (SQLException e) {
                throw new IllegalStateException("Erro ao iniciar transação MySQL", e);
            }
        }
        @Override public synchronized void commit() {
            if (!ativa) throw new IllegalStateException("Nenhuma transação ativa");
            try {
                getConnection().commit();
                ativa = false;
                restaurarAutoCommit();
            } catch (SQLException e) {
                // Mantém ativa para permitir rollback de um commit que falhou.
                throw new IllegalStateException("Erro no commit MySQL", e);
            }
        }
        @Override public synchronized void rollback() {
            if (!ativa) return;
            try {
                getConnection().rollback();
                ativa = false;
                restaurarAutoCommit();
            } catch (SQLException e) {
                try { close(); } catch (RuntimeException fechamento) { e.addSuppressed(fechamento); }
                throw new IllegalStateException("Erro no rollback MySQL", e);
            }
        }
        private void restaurarAutoCommit() {
            try { getConnection().setAutoCommit(true); }
            catch (SQLException e) {
                // A transação já terminou. Descarta a conexão sem dizer que o commit falhou.
                System.err.println("Conexão descartada após transação: " + e.getMessage());
                try { close(); } catch (RuntimeException fechamento) { System.err.println(fechamento); }
            }
        }
        @Override public synchronized boolean isAtiva() { return ativa; }
        @Override public synchronized void close() {
            try { if (connection != null) connection.close(); }
            catch (SQLException e) { throw new IllegalStateException("Erro ao fechar conexão", e); }
            finally { connection = null; ativa = false; }
        }
    }

    public static class MysqlAcervoDeLivros implements AcervoDeLivros {
        private final MysqlUnidadeTrabalho uow;
        public MysqlAcervoDeLivros(MysqlUnidadeTrabalho uow) { this.uow = uow; }
        @Override public UnidadeTrabalho unidadeTrabalho() {
            synchronized (uow) {
                return uow;
            }
        }
        public boolean usaMesmaTransacao(UnidadeTrabalho outra) { return this.uow == outra; }

        @Override
        public void salvar(Livro livro) {
            synchronized (uow) {

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
        }

        @Override
        public Optional<Livro> consultarLivro(int idLivro) {
            synchronized (uow) {

                String sql = "SELECT id_livro, titulo, estado, id_reservado_estudante, num_renovacoes, excluido FROM livros WHERE id_livro = ?" + (uow.isAtiva() ? " FOR UPDATE" : "");
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
        }

        @Override
        public List<Livro> listarTodos() {
            synchronized (uow) {

                String sql = "SELECT id_livro, titulo, estado, id_reservado_estudante, num_renovacoes, excluido FROM livros";
                List<Livro> lista = new ArrayList<>();
                try (PreparedStatement ps = uow.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Livro l = new Livro(rs.getInt("id_livro"), rs.getString("titulo"));
                        l.reconstituirEstadoPorNome(rs.getString("estado"));
                        l.setNumRenovacoes(rs.getInt("num_renovacoes"));
                        int reservado = rs.getInt("id_reservado_estudante");
                        if (!rs.wasNull()) l.setIdReservadoParaEstudante(reservado);
                        if (rs.getBoolean("excluido")) l.marcarExcluido();
                        lista.add(l);
                    }
                    return lista;
                } catch (SQLException e) {
                    throw new IllegalStateException("Erro JDBC ao listar Livros: " + e.getMessage(), e);
                }

            }
        }

        @Override
        public void excluirLivro(int idLivro) {
            synchronized (uow) {

                try (PreparedStatement ps = uow.getConnection().prepareStatement("DELETE FROM livros WHERE id_livro = ?")) {
                    ps.setInt(1, idLivro);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new IllegalStateException("Erro JDBC ao remover Livro: " + e.getMessage(), e);
                }

            }
        }
    }

    public static class MysqlMovimentacao implements Movimentacao {
        private final MysqlUnidadeTrabalho uow;
        public MysqlMovimentacao(MysqlUnidadeTrabalho uow) { this.uow = uow; }
        @Override public UnidadeTrabalho unidadeTrabalho() {
            synchronized (uow) {
                return uow;
            }
        }
        public boolean usaMesmaTransacao(UnidadeTrabalho outra) { return this.uow == outra; }

        @Override
        public void inserirReserva(Reserva r) {
            synchronized (uow) {

                String sql = "INSERT INTO reservas (id_livro, id_estudante, data_reserva, ativa) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE data_reserva=VALUES(data_reserva), ativa=VALUES(ativa)";
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
        }

        @Override
        public Optional<Reserva> consultarReserva(int idLivro, int idEstudante) {
            synchronized (uow) {

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
        }

        @Override
        public void removerReserva(int idLivro, int idEstudante) {
            synchronized (uow) {

                String sql = "DELETE FROM reservas WHERE id_livro = ? AND id_estudante = ?";
                try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                    ps.setInt(1, idLivro);
                    ps.setInt(2, idEstudante);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new IllegalStateException("Erro JDBC ao remover Reserva: " + e.getMessage(), e);
                }

            }
        }

        @Override
        public void inserirEmprestimo(Emprestimo emp) {
            synchronized (uow) {

                boolean novo = emp.getIdEmprestimo() == 0;
                String sql = novo
                ? "INSERT INTO emprestimos (id_livro, id_estudante, data_inicio, data_vencimento, estado, devolvido) VALUES (?, ?, ?, ?, ?, ?)"
                : "UPDATE emprestimos SET id_livro=?, id_estudante=?, data_inicio=?, data_vencimento=?, estado=?, devolvido=? WHERE id_emprestimo=?";
                try (PreparedStatement ps = uow.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, emp.getIdLivro()); ps.setInt(2, emp.getIdEstudante());
                    ps.setDate(3, Date.valueOf(emp.getDataInicio()));
                    ps.setDate(4, Date.valueOf(emp.getDataVencimento()));
                    ps.setString(5, emp.getEstado()); ps.setBoolean(6, emp.isDevolvido());
                    if (!novo) ps.setLong(7, emp.getIdEmprestimo());
                    if (ps.executeUpdate() != 1) throw new SQLException("Empréstimo não encontrado para atualização");
                    if (novo) {
                        try (ResultSet chaves = ps.getGeneratedKeys()) {
                            if (!chaves.next()) throw new SQLException("Chave do empréstimo não retornada");
                            emp.definirIdPersistido(chaves.getLong(1));
                        }
                    }
                } catch (SQLException e) {
                    throw new IllegalStateException("Erro JDBC ao salvar Empréstimo: " + e.getMessage(), e);
                }

            }
        }

        @Override
        public Optional<Emprestimo> consultarEmprestimo(int idLivro, int idEstudante) {
            synchronized (uow) {

                String sql = "SELECT id_emprestimo, data_inicio, data_vencimento, estado, devolvido FROM emprestimos WHERE id_livro = ? AND id_estudante = ? AND devolvido = FALSE ORDER BY id_emprestimo DESC LIMIT 1";
                try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                    ps.setInt(1, idLivro);
                    ps.setInt(2, idEstudante);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) return Optional.empty();
                        Emprestimo emp = new Emprestimo(idLivro, idEstudante,
                        rs.getDate("data_inicio").toLocalDate(), rs.getDate("data_vencimento").toLocalDate());
                        emp.definirIdPersistido(rs.getLong("id_emprestimo"));
                        emp.reconstituirSituacao(rs.getString("estado"), rs.getBoolean("devolvido"));
                        return Optional.of(emp);
                    }
                } catch (SQLException e) {
                    throw new IllegalStateException("Erro JDBC ao buscar Empréstimo: " + e.getMessage(), e);
                }

            }
        }

        @Override
        public void inserirDevolucao(Devolucao d) {
            synchronized (uow) {

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
        }

        @Override
        public Optional<Devolucao> consultarDevolucao(int idLivro, int idEstudante) {
            synchronized (uow) {

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
        }

        @Override
        public void salvarBoleto(BoletoMulta b) {
            synchronized (uow) {

                String sql = "INSERT INTO boletos_multa (id_cobranca, id_livro, id_estudante, valor, politica_aplicada, situacao) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE situacao=VALUES(situacao)";
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
        }

        @Override
        public List<BoletoMulta> listarBoletos() {
            synchronized (uow) {

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
    }

    public static class MysqlRelacionamento implements Relacionamento {
        private final MysqlUnidadeTrabalho uow;
        public MysqlRelacionamento(MysqlUnidadeTrabalho uow) { this.uow = uow; }
        @Override public UnidadeTrabalho unidadeTrabalho() {
            synchronized (uow) {
                return uow;
            }
        }
        public boolean usaMesmaTransacao(UnidadeTrabalho outra) { return this.uow == outra; }

        @Override
        public void inserirPessoa(Pessoa p) {
            synchronized (uow) {

                String sql = "INSERT INTO pessoas (cpf, nome, sobrenome) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE nome=VALUES(nome), sobrenome=VALUES(sobrenome)";
                try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                    ps.setString(1, p.cpf());
                    ps.setString(2, p.nome());
                    ps.setString(3, p.sobrenome());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new IllegalStateException("Erro JDBC ao salvar Pessoa: " + e.getMessage(), e);
                }

            }
        }

        @Override
        public Optional<Pessoa> consultarPessoa(String cpf) {
            synchronized (uow) {

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
        }

        @Override
        public void excluirPessoa(String cpf) {
            synchronized (uow) {

                try (PreparedStatement ps = uow.getConnection().prepareStatement("DELETE FROM pessoas WHERE cpf = ?")) {
                    ps.setString(1, cpf);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new IllegalStateException("Erro JDBC ao remover Pessoa: " + e.getMessage(), e);
                }

            }
        }

        @Override
        public void inserirEstudante(Estudante est) {
            synchronized (uow) {

                String sql = "INSERT INTO estudantes (id_estudante, cpf, matricula) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE cpf=VALUES(cpf), matricula=VALUES(matricula)";
                try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                    ps.setInt(1, est.idEstudante());
                    ps.setString(2, est.cpf());
                    ps.setString(3, est.matricula());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new IllegalStateException("Erro JDBC ao salvar Estudante: " + e.getMessage(), e);
                }

            }
        }

        @Override
        public Optional<Estudante> consultarEstudante(int idEstudante) {
            synchronized (uow) {

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
        }

        @Override
        public void excluirEstudante(int idEstudante) {
            synchronized (uow) {

                try (PreparedStatement ps = uow.getConnection().prepareStatement("DELETE FROM estudantes WHERE id_estudante = ?")) {
                    ps.setInt(1, idEstudante);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new IllegalStateException("Erro JDBC ao remover Estudante: " + e.getMessage(), e);
                }

            }
        }

        @Override
        public void inserirAtendente(Atendente at) {
            synchronized (uow) {

                String sql = "INSERT INTO atendentes (id_atendente, cpf) VALUES (?, ?) ON DUPLICATE KEY UPDATE cpf=VALUES(cpf)";
                try (PreparedStatement ps = uow.getConnection().prepareStatement(sql)) {
                    ps.setInt(1, at.idAtendente());
                    ps.setString(2, at.cpf());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new IllegalStateException("Erro JDBC ao salvar Atendente: " + e.getMessage(), e);
                }

            }
        }

        @Override
        public Optional<Atendente> consultarAtendente(int idAtendente) {
            synchronized (uow) {

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
        }

        @Override
        public void excluirAtendente(int idAtendente) {
            synchronized (uow) {

                try (PreparedStatement ps = uow.getConnection().prepareStatement("DELETE FROM atendentes WHERE id_atendente = ?")) {
                    ps.setInt(1, idAtendente);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new IllegalStateException("Erro JDBC ao remover Atendente: " + e.getMessage(), e);
                }

            }
        }
    }
}
