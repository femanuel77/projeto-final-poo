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
                // Tenta carregar o driver e GRITA se não achar
                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException e) {
                    // SE ENTRAR AQUI, O .JAR NÃO ESTÁ NO CLASS PATH MESMO!
                    javax.swing.JOptionPane.showMessageDialog(null, 
                        "ERRO CRÍTICO: O arquivo .jar do SQLite não foi encontrado!\n" +
                        "Verifique 'Referenced Libraries' no VS Code.");
                    throw new RuntimeException("Driver SQLite não encontrado no Classpath!");
                }

                connection = DriverManager.getConnection(URL);
                System.out.println("Conexão estabelecida com sucesso!");
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