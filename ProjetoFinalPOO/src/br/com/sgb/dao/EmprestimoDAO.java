package br.com.sgb.dao;

import br.com.sgb.model.Emprestimo;
import br.com.sgb.util.ConnectionFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmprestimoDAO {
    private Connection connection;

    public EmprestimoDAO() {
        this.connection = ConnectionFactory.getConnection();
    }

    // REGISTRAR EMPRÉSTIMO
    public void registrarEmprestimo(Emprestimo emprestimo) {
        String sql = "INSERT INTO emprestimo (data_emprestimo, data_prevista_devolucao, status, leitor_id, exemplar_id) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, emprestimo.getDataEmprestimo());
            stmt.setString(2, emprestimo.getDataPrevistaDevolucao());
            stmt.setString(3, "EM_ANDAMENTO");
            stmt.setInt(4, emprestimo.getLeitorId());
            stmt.setInt(5, emprestimo.getExemplarId());
            stmt.execute();

            // Vibe Coding: Já atualiza o status do Exemplar para indisponível
            // (Idealmente faríamos numa transaction, mas aqui é foco na entrega)
            new ExemplarDAO().atualizarStatus(emprestimo.getExemplarId(), "EMPRESTADO");
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar empréstimo: " + e.getMessage());
        }
    }

    // DEVOLVER LIVRO
    public void registrarDevolucao(int emprestimoId, String dataDevolucao, int exemplarId) {
        String sql = "UPDATE emprestimo SET data_devolucao=?, status=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, dataDevolucao);
            stmt.setString(2, "FINALIZADO");
            stmt.setInt(3, emprestimoId);
            stmt.execute();

            // Libera o exemplar
            new ExemplarDAO().atualizarStatus(exemplarId, "DISPONIVEL");
            
        } catch (SQLException e) {
            throw new RuntimeException("Erro na devolução: " + e.getMessage());
        }
    }

    // LISTAR (JOIN para pegar nome do Leitor e Título do Livro)
    public List<Emprestimo> listarTodos() {
        String sql = """
            SELECT e.*, l.nome as nome_leitor, liv.titulo as titulo_livro 
            FROM emprestimo e
            JOIN leitor l ON e.leitor_id = l.id
            JOIN exemplar ex ON e.exemplar_id = ex.id
            JOIN livro liv ON ex.livro_id = liv.id
        """;
        
        List<Emprestimo> lista = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Emprestimo emp = new Emprestimo();
                emp.setId(rs.getInt("id"));
                emp.setDataEmprestimo(rs.getString("data_emprestimo"));
                emp.setDataPrevistaDevolucao(rs.getString("data_prevista_devolucao"));
                emp.setDataDevolucao(rs.getString("data_devolucao"));
                emp.setStatus(rs.getString("status"));
                emp.setLeitorId(rs.getInt("leitor_id"));
                emp.setExemplarId(rs.getInt("exemplar_id"));
                
                // Dados extras para exibição
                emp.setNomeLeitor(rs.getString("nome_leitor"));
                emp.setTituloLivro(rs.getString("titulo_livro"));
                
                lista.add(emp);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar empréstimos: " + e.getMessage());
        }
        return lista;
    }
}
