package br.com.sgb.view;

import br.com.sgb.dao.LivroDAO;
import br.com.sgb.model.Livro;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PainelLivro extends JPanel {
    private JTextField txtIsbn, txtTitulo, txtEdicao, txtEditora, txtAno;
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private LivroDAO livroDAO;

    public PainelLivro() {
        this.livroDAO = new LivroDAO();
        setLayout(new BorderLayout());

        // --- Formulário de Cadastro (Topo) ---
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastrar Novo Livro"));

        txtIsbn = new JTextField();
        txtTitulo = new JTextField();
        txtEdicao = new JTextField();
        txtEditora = new JTextField();
        txtAno = new JTextField();

        formPanel.add(new JLabel("ISBN:")); formPanel.add(txtIsbn);
        formPanel.add(new JLabel("Título:")); formPanel.add(txtTitulo);
        formPanel.add(new JLabel("Edição (nº):")); formPanel.add(txtEdicao);
        formPanel.add(new JLabel("Editora:")); formPanel.add(txtEditora);
        formPanel.add(new JLabel("Ano Publicação:")); formPanel.add(txtAno);

        JButton btnSalvar = new JButton("Salvar Livro");
        btnSalvar.addActionListener(e -> salvarLivro());
        
        JPanel topo = new JPanel(new BorderLayout());
        topo.add(formPanel, BorderLayout.CENTER);
        topo.add(btnSalvar, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        // --- Tabela de Listagem (Centro) ---
        modeloTabela = new DefaultTableModel(new Object[]{"ID", "ISBN", "Título", "Editora"}, 0);
        tabela = new JTable(modeloTabela);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // --- Botão de Atualizar (Baixo) ---
        JButton btnAtualizar = new JButton("Atualizar Lista");
        btnAtualizar.addActionListener(e -> carregarTabela());
        add(btnAtualizar, BorderLayout.SOUTH);

        // Carrega dados ao abrir
        carregarTabela();
    }

    private void salvarLivro() {
        try {
            // Validação simples (Vibe Coding)
            if(txtTitulo.getText().isEmpty() || txtIsbn.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha pelo menos ISBN e Título!");
                return;
            }

            Livro l = new Livro();
            l.setIsbn(txtIsbn.getText());
            l.setTitulo(txtTitulo.getText());
            l.setEdicao(Integer.parseInt(txtEdicao.getText())); // Cuidado: pode dar erro se não for número
            l.setEditora(txtEditora.getText());
            l.setAnoPublicacao(Integer.parseInt(txtAno.getText()));

            livroDAO.salvar(l);
            JOptionPane.showMessageDialog(this, "Livro salvo com sucesso!");
            
            // Limpar campos
            txtIsbn.setText(""); txtTitulo.setText(""); txtEdicao.setText(""); 
            txtEditora.setText(""); txtAno.setText("");
            
            carregarTabela(); // Atualiza a lista visualmente
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Edição e Ano devem ser números!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + e.getMessage());
        }
    }

    private void carregarTabela() {
        modeloTabela.setRowCount(0); // Limpa tabela
        List<Livro> livros = livroDAO.listarTodos();
        for (Livro l : livros) {
            modeloTabela.addRow(new Object[]{l.getId(), l.getIsbn(), l.getTitulo(), l.getEditora()});
        }
    }
}