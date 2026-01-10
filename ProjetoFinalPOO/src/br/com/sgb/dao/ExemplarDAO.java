package br.com.sgb.dao;

import br.com.sgb.model.Exemplar;
import br.com.sgb.util.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExemplarDAO {
    
    private Connection connection;

    public ExemplarDAO() {
        this.connection = ConnectionFactory.getConnection();
    }

    public void salvar(Exemplar exemplar) {
        // Aqui garantimos o relacionamento 1:N salvando o ID do livro
        String sql = "INSERT INTO exemplar (codigo_barra, status, livro_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, exemplar.getCodigoBarra());
            stmt.setString(2, exemplar.getStatus());
            stmt.setInt(3, exemplar.getLivroId()); // Chave estrangeira
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar exemplar: " + e.getMessage());
        }
    }

    public List<Exemplar> listarPorLivro(int livroId) {
        String sql = "SELECT * FROM exemplar WHERE livro_id = ?";
        List<Exemplar> exemplares = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, livroId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Exemplar e = new Exemplar();
                e.setId(rs.getInt("id"));
                e.setCodigoBarra(rs.getString("codigo_barra"));
                e.setStatus(rs.getString("status"));
                e.setLivroId(rs.getInt("livro_id"));
                exemplares.add(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar exemplares: " + e.getMessage());
        }
        return exemplares;
    }
    
    // Método extra para mudar status (Útil para empréstimo)
    public void atualizarStatus(int id, String novoStatus) {
        String sql = "UPDATE exemplar SET status=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, novoStatus);
            stmt.setInt(2, id);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status: " + e.getMessage());
        }
    }
}