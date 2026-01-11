package br.com.sgb.dao;

import br.com.sgb.model.Leitor;
import br.com.sgb.util.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeitorDAO {
    private Connection connection;

    public LeitorDAO() {
        this.connection = ConnectionFactory.getConnection();
    }

    public void salvar(Leitor leitor) {
        String sql = "INSERT INTO leitor (cpf, nome, email, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, leitor.getCpf());
            stmt.setString(2, leitor.getNome());
            stmt.setString(3, leitor.getEmail());
            stmt.setString(4, leitor.getStatus());
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar leitor: " + e.getMessage());
        }
    }
    
    // --- NOVO MÉTODO: ATUALIZAR (UPDATE) ---
    public void atualizar(Leitor leitor) {
        String sql = "UPDATE leitor SET cpf=?, nome=?, email=?, status=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, leitor.getCpf());
            stmt.setString(2, leitor.getNome());
            stmt.setString(3, leitor.getEmail());
            stmt.setString(4, leitor.getStatus());
            stmt.setInt(5, leitor.getId());
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar dados: " + e.getMessage());
        }
    }

    public List<Leitor> listarTodos() {
        String sql = "SELECT * FROM leitor";
        List<Leitor> leitores = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Leitor l = new Leitor();
                l.setId(rs.getInt("id"));
                l.setCpf(rs.getString("cpf"));
                l.setNome(rs.getString("nome"));
                l.setEmail(rs.getString("email"));
                l.setStatus(rs.getString("status"));
                leitores.add(l);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar leitores: " + e.getMessage());
        }
        return leitores;
    }
    
    public Leitor buscarPorCpf(String cpf) {
        String sql = "SELECT * FROM leitor WHERE cpf = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Leitor l = new Leitor();
                l.setId(rs.getInt("id"));
                l.setCpf(rs.getString("cpf"));
                l.setNome(rs.getString("nome"));
                l.setEmail(rs.getString("email"));
                l.setStatus(rs.getString("status"));
                return l;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void atualizarStatus(int id, String novoStatus) {
        String sql = "UPDATE leitor SET status=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, novoStatus);
            stmt.setInt(2, id);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status: " + e.getMessage());
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM leitor WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Não é possível excluir: Leitor possui empréstimos vinculados!");
        }
    }
}