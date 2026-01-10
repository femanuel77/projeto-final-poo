-- Tabela de Leitores
CREATE TABLE IF NOT EXISTS leitor (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cpf TEXT NOT NULL UNIQUE,
    nome TEXT NOT NULL,
    email TEXT NOT NULL,
    status TEXT NOT NULL -- 'ATIVO', 'INATIVO', 'SUSPENSO'
);

-- Tabela de Livros
CREATE TABLE IF NOT EXISTS livro (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    isbn TEXT NOT NULL UNIQUE,
    titulo TEXT NOT NULL,
    edicao INTEGER,
    editora TEXT,
    ano_publicacao INTEGER
);

-- Tabela de Exemplares (Relação 1:N com Livro)
CREATE TABLE IF NOT EXISTS exemplar (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_barra TEXT NOT NULL UNIQUE,
    status TEXT NOT NULL, -- 'DISPONIVEL', 'EMPRESTADO', 'INDISPONIVEL'
    livro_id INTEGER NOT NULL,
    FOREIGN KEY (livro_id) REFERENCES livro(id)
);

-- Tabela de Empréstimos (Relação com Leitor e Exemplar)
CREATE TABLE IF NOT EXISTS emprestimo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    data_emprestimo TEXT NOT NULL,
    data_prevista_devolucao TEXT NOT NULL,
    data_devolucao TEXT,
    status TEXT NOT NULL, -- 'EM_ANDAMENTO', 'DEVOLVIDO', 'ATRASADO'
    leitor_id INTEGER NOT NULL,
    exemplar_id INTEGER NOT NULL,
    FOREIGN KEY (leitor_id) REFERENCES leitor(id),
    FOREIGN KEY (exemplar_id) REFERENCES exemplar(id)
);