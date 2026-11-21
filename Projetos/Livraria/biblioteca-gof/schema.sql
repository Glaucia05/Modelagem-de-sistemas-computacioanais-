CREATE DATABASE IF NOT EXISTS biblioteca_ib2b CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biblioteca_ib2b;

CREATE TABLE IF NOT EXISTS pessoas (
    cpf VARCHAR(14) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    sobrenome VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS estudantes (
    id_estudante INT PRIMARY KEY,
    cpf VARCHAR(14) NOT NULL,
    matricula VARCHAR(50) NOT NULL,
    CONSTRAINT fk_estudante_pessoa FOREIGN KEY (cpf) REFERENCES pessoas(cpf)
);

CREATE TABLE IF NOT EXISTS atendentes (
    id_atendente INT PRIMARY KEY,
    cpf VARCHAR(14) NOT NULL,
    CONSTRAINT fk_atendente_pessoa FOREIGN KEY (cpf) REFERENCES pessoas(cpf)
);

CREATE TABLE IF NOT EXISTS livros (
    id_livro INT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    id_reservado_estudante INT NULL,
    num_renovacoes INT NOT NULL DEFAULT 0,
    excluido BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS reservas (
    id_livro INT NOT NULL,
    id_estudante INT NOT NULL,
    data_reserva DATE NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_livro, id_estudante),
    CONSTRAINT fk_reserva_livro FOREIGN KEY (id_livro) REFERENCES livros(id_livro),
    CONSTRAINT fk_reserva_estudante FOREIGN KEY (id_estudante) REFERENCES estudantes(id_estudante)
);

CREATE TABLE IF NOT EXISTS emprestimos (
    id_emprestimo INT AUTO_INCREMENT PRIMARY KEY,
    id_livro INT NOT NULL,
    id_estudante INT NOT NULL,
    data_inicio DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    estado VARCHAR(30) NOT NULL,
    devolvido BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_emprestimo_livro FOREIGN KEY (id_livro) REFERENCES livros(id_livro),
    CONSTRAINT fk_emprestimo_estudante FOREIGN KEY (id_estudante) REFERENCES estudantes(id_estudante)
);

CREATE TABLE IF NOT EXISTS devolucoes (
    id_devolucao INT AUTO_INCREMENT PRIMARY KEY,
    id_livro INT NOT NULL,
    id_estudante INT NOT NULL,
    data_devolucao DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    CONSTRAINT fk_devolucao_livro FOREIGN KEY (id_livro) REFERENCES livros(id_livro),
    CONSTRAINT fk_devolucao_estudante FOREIGN KEY (id_estudante) REFERENCES estudantes(id_estudante)
);

CREATE TABLE IF NOT EXISTS boletos_multa (
    id_cobranca VARCHAR(60) PRIMARY KEY,
    id_livro INT NOT NULL,
    id_estudante INT NOT NULL,
    valor DECIMAL(10, 2) NOT NULL,
    politica_aplicada VARCHAR(60) NOT NULL,
    situacao VARCHAR(30) NOT NULL,
    CONSTRAINT fk_boleto_livro FOREIGN KEY (id_livro) REFERENCES livros(id_livro),
    CONSTRAINT fk_boleto_estudante FOREIGN KEY (id_estudante) REFERENCES estudantes(id_estudante)
);
