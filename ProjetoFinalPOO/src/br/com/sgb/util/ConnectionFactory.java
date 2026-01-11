package br.com.sgb.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {

    private static Connection connection;
    private static final String URL = "jdbc:sqlite:biblioteca.db";

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                connection = DriverManager.getConnection(URL);
                
                // Ativa integridade referencial (FOREIGN KEYS)
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
                
                initializeDatabase();
                populateInitialData(); // Carga de dados automática
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar: " + e.getMessage());
        }
        return connection;
    }

    private static void initializeDatabase() {
        String sql = """
            CREATE TABLE IF NOT EXISTS leitor (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cpf TEXT NOT NULL UNIQUE,
                nome TEXT NOT NULL,
                email TEXT NOT NULL,
                status TEXT NOT NULL
            );
            CREATE TABLE IF NOT EXISTS livro (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                isbn TEXT NOT NULL UNIQUE,
                titulo TEXT NOT NULL,
                edicao INTEGER,
                editora TEXT,
                ano_publicacao INTEGER
            );
            CREATE TABLE IF NOT EXISTS exemplar (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                codigo_barra TEXT NOT NULL UNIQUE,
                status TEXT NOT NULL,
                livro_id INTEGER NOT NULL,
                FOREIGN KEY (livro_id) REFERENCES livro(id) ON DELETE CASCADE
            );
            CREATE TABLE IF NOT EXISTS emprestimo (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                data_emprestimo TEXT NOT NULL,
                data_prevista_devolucao TEXT NOT NULL,
                data_devolucao TEXT,
                status TEXT NOT NULL,
                leitor_id INTEGER NOT NULL,
                exemplar_id INTEGER NOT NULL,
                FOREIGN KEY (leitor_id) REFERENCES leitor(id) ON DELETE RESTRICT,
                FOREIGN KEY (exemplar_id) REFERENCES exemplar(id) ON DELETE RESTRICT
            );
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- CARGA DE DADOS INICIAL (SEED) ---
    private static void populateInitialData() {
        try (Statement stmt = connection.createStatement()) {
            // Verifica se já tem livros. Se tiver, não faz nada.
            ResultSet rs = stmt.executeQuery("SELECT count(*) as qtd FROM livro");
            if (rs.next() && rs.getInt("qtd") > 0) return;

            System.out.println("Banco vazio detectado. Inserindo dados de exemplo...");

            // 1. Inserir Leitores
            stmt.executeUpdate("INSERT INTO leitor (cpf, nome, email, status) VALUES ('123.456.789-00', 'Ada Lovelace', 'ada@history.com', 'ATIVO')");
            stmt.executeUpdate("INSERT INTO leitor (cpf, nome, email, status) VALUES ('111.222.333-44', 'Alan Turing', 'alan@enigma.com', 'ATIVO')");
            stmt.executeUpdate("INSERT INTO leitor (cpf, nome, email, status) VALUES ('999.888.777-66', 'Grace Hopper', 'grace@cobol.com', 'BLOQUEADO')");

            // 2. Inserir Livros Reais
            // Livro 1: Clean Code
            stmt.executeUpdate("INSERT INTO livro (isbn, titulo, edicao, editora, ano_publicacao) VALUES ('9788576082675', 'Clean Code', 1, 'Alta Books', 2009)");
            // Livro 2: Dom Casmurro
            stmt.executeUpdate("INSERT INTO livro (isbn, titulo, edicao, editora, ano_publicacao) VALUES ('9788594318591', 'Dom Casmurro', 1, 'Principis', 1899)");
            // Livro 3: Design Patterns
            stmt.executeUpdate("INSERT INTO livro (isbn, titulo, edicao, editora, ano_publicacao) VALUES ('9780201633610', 'Design Patterns (GoF)', 1, 'Addison-Wesley', 1994)");
            // Livro 4: O Senhor dos Anéis
            stmt.executeUpdate("INSERT INTO livro (isbn, titulo, edicao, editora, ano_publicacao) VALUES ('9788595084742', 'O Senhor dos Anéis', 1, 'HarperCollins', 1954)");

            // 3. Inserir Exemplares (Cópia Física) para os livros acima
            // IDs gerados automaticamente são sequenciais (1, 2, 3, 4)
            
            // Exemplares do Clean Code (ID 1)
            stmt.executeUpdate("INSERT INTO exemplar (codigo_barra, status, livro_id) VALUES ('9788576082675-1', 'DISPONIVEL', 1)");
            stmt.executeUpdate("INSERT INTO exemplar (codigo_barra, status, livro_id) VALUES ('9788576082675-2', 'DISPONIVEL', 1)");

            // Exemplares de Dom Casmurro (ID 2)
            stmt.executeUpdate("INSERT INTO exemplar (codigo_barra, status, livro_id) VALUES ('9788594318591-1', 'DISPONIVEL', 2)");
            
            // Exemplares de Design Patterns (ID 3)
            stmt.executeUpdate("INSERT INTO exemplar (codigo_barra, status, livro_id) VALUES ('9780201633610-1', 'DISPONIVEL', 3)");
            stmt.executeUpdate("INSERT INTO exemplar (codigo_barra, status, livro_id) VALUES ('9780201633610-2', 'DISPONIVEL', 3)");
            stmt.executeUpdate("INSERT INTO exemplar (codigo_barra, status, livro_id) VALUES ('9780201633610-3', 'DISPONIVEL', 3)");

            // Exemplares de Senhor dos Anéis (ID 4)
            stmt.executeUpdate("INSERT INTO exemplar (codigo_barra, status, livro_id) VALUES ('9788595084742-1', 'DISPONIVEL', 4)");

            System.out.println("Carga de dados inicial concluída com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro na carga inicial: " + e.getMessage());
        }
    }
}