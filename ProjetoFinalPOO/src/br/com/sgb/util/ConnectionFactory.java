package br.com.sgb.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {

    // Padrão Singleton: Uma única instância
    private static Connection connection;
    // URL do banco: vai criar um arquivo "biblioteca.db" na pasta do projeto
    private static final String URL = "jdbc:sqlite:biblioteca.db";

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Carrega o driver (necessário em algumas versões do Java)
                // Certifique-se de ter o .jar do sqlite no classpath
                // Class.forName("org.sqlite.JDBC"); 
                connection = DriverManager.getConnection(URL);
                System.out.println("Conexão estabelecida com sucesso!");
                
                // Inicializa o banco (Cria tabelas se não existirem)
                initializeDatabase();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao conectar com o banco: " + e.getMessage());
        }
        return connection;
    }

    private static void initializeDatabase() {
        // Vibe Coding: Criação automática das tabelas para não precisar de SQL externo
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
                FOREIGN KEY (livro_id) REFERENCES livro(id)
            );
            CREATE TABLE IF NOT EXISTS emprestimo (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                data_emprestimo TEXT NOT NULL,
                data_prevista_devolucao TEXT NOT NULL,
                data_devolucao TEXT,
                status TEXT NOT NULL,
                leitor_id INTEGER NOT NULL,
                exemplar_id INTEGER NOT NULL,
                FOREIGN KEY (leitor_id) REFERENCES leitor(id),
                FOREIGN KEY (exemplar_id) REFERENCES exemplar(id)
            );
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}