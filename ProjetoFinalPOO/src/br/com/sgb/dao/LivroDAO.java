package br.com.sgb.dao;

import br.com.sgb.model.Livro;
import br.com.sgb.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LivroDAO {

    private Connection connection;

    public LivroDAO() {
        this.connection = ConnectionFactory.getConnection();
    }

    public void salvar(Livro livro) {
        String sql = "INSERT INTO livro (isbn, titulo, edicao, editora, ano_publicacao) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, livro.getIsbn());
            stmt.setString(2, livro.getTitulo());
            stmt.setInt(3, livro.getEdicao());
            stmt.setString(4, livro.getEditora());
            stmt.setInt(5, livro.getAnoPublicacao());
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar livro: " + e.getMessage());
        }
    }

    public List<Livro> listarTodos() {
        String sql = "SELECT * FROM livro";
        List<Livro> livros = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Livro l = new Livro();
                l.setId(rs.getInt("id"));
                l.setIsbn(rs.getString("isbn"));
                l.setTitulo(rs.getString("titulo"));
                l.setEdicao(rs.getInt("edicao"));
                l.setEditora(rs.getString("editora"));
                l.setAnoPublicacao(rs.getInt("ano_publicacao"));
                livros.add(l);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar livros: " + e.getMessage());
        }
        return livros;
    }

    // --- NOVO: Deletar Livro e seus Exemplares ---
    public void deletar(int id) {
        // Primeiro deletamos os exemplares (Cascade manual)
        // OBS: Se tiver empréstimo ativo, o banco vai bloquear por causa da FK, o que é BOM (Segurança)
        String sqlExemplar = "DELETE FROM exemplar WHERE livro_id=?";
        String sqlLivro = "DELETE FROM livro WHERE id=?";
        
        try {
            // Transaction simples
            try (PreparedStatement stmtEx = connection.prepareStatement(sqlExemplar)) {
                stmtEx.setInt(1, id);
                stmtEx.execute();
            }
            try (PreparedStatement stmtLiv = connection.prepareStatement(sqlLivro)) {
                stmtLiv.setInt(1, id);
                stmtLiv.execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Não é possível excluir: O livro possui empréstimos registrados!");
        }
    }
}