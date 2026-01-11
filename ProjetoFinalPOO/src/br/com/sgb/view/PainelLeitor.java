package br.com.sgb.view;

import br.com.sgb.dao.LeitorDAO;
import br.com.sgb.model.Leitor;
import javax.swing.*;
import javax.swing.text.MaskFormatter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.regex.Pattern;

public class PainelLeitor extends JPanel {
    private JFormattedTextField txtCpf;
    private JTextField txtNome, txtEmail;
    private JComboBox<String> cbStatus;
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private LeitorDAO leitorDAO;

    public PainelLeitor() {
        leitorDAO = new LeitorDAO();
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Gerenciar Leitor"));

        try {
            MaskFormatter mascaraCpf = new MaskFormatter("###.###.###-##");
            mascaraCpf.setPlaceholderCharacter('_');
            txtCpf = new JFormattedTextField(mascaraCpf);
        } catch (Exception e) {
            txtCpf = new JFormattedTextField();
        }

        txtNome = new JTextField();
        // NOVO: Bloqueia números no nome
        bloquearNumeros(txtNome);
        
        txtEmail = new JTextField();
        cbStatus = new JComboBox<>(new String[]{"ATIVO", "BLOQUEADO"});

        formPanel.add(new JLabel("CPF (Obrigatório) * :")); formPanel.add(txtCpf);
        formPanel.add(new JLabel("Nome (Sem números) * :")); formPanel.add(txtNome);
        formPanel.add(new JLabel("Email * :")); formPanel.add(txtEmail);
        formPanel.add(new JLabel("Status Inicial:")); formPanel.add(cbStatus);

        JButton btnSalvar = new JButton("Cadastrar Leitor");
        btnSalvar.addActionListener(e -> salvar());

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(formPanel, BorderLayout.CENTER);
        topo.add(btnSalvar, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        modeloTabela = new DefaultTableModel(new Object[]{"ID", "Nome", "CPF", "Email", "Status"}, 0);
        tabela = new JTable(modeloTabela);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        
        JPanel rodape = new JPanel();
        JButton btnAlterarStatus = new JButton("Bloquear/Desbloquear");
        btnAlterarStatus.setBackground(new Color(255, 200, 200));
        btnAlterarStatus.addActionListener(e -> alternarStatus());

        JButton btnExcluir = new JButton("🗑️ Excluir Leitor");
        btnExcluir.setBackground(new Color(200, 100, 100));
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.addActionListener(e -> excluirLeitor());
        
        JButton btnRefresh = new JButton("Atualizar Lista");
        btnRefresh.addActionListener(e -> carregar());
        
        rodape.add(btnAlterarStatus); rodape.add(btnExcluir); rodape.add(btnRefresh);
        add(rodape, BorderLayout.SOUTH);

        carregar();
    }

    private void salvar() {
        try {
            String cpfOriginal = (String) txtCpf.getValue();
            if (cpfOriginal == null || cpfOriginal.contains("_")) {
                JOptionPane.showMessageDialog(this, "CPF inválido! Preencha os 11 dígitos.");
                return;
            }
            
            if (txtNome.getText().trim().isEmpty() || txtEmail.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome e Email são obrigatórios!");
                return;
            }
            
            String email = txtEmail.getText();
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (!Pattern.matches(emailRegex, email)) {
                JOptionPane.showMessageDialog(this, "E-mail inválido! Formato: user@domain.com");
                return;
            }

            Leitor l = new Leitor(cpfOriginal, txtNome.getText(), email, (String) cbStatus.getSelectedItem());
            leitorDAO.salvar(l);
            JOptionPane.showMessageDialog(this, "Leitor cadastrado!");
            txtCpf.setValue(null); txtNome.setText(""); txtEmail.setText("");
            carregar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar (CPF já existe?): " + e.getMessage());
        }
    }

    // --- MÉTODOS DE CONTROLE ---
    private void alternarStatus() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um leitor!");
            return;
        }
        int id = (int) tabela.getValueAt(linha, 0);
        String statusAtual = (String) tabela.getValueAt(linha, 4);
        String novoStatus = statusAtual.equals("ATIVO") ? "BLOQUEADO" : "ATIVO";
        leitorDAO.atualizarStatus(id, novoStatus);
        JOptionPane.showMessageDialog(this, "Status alterado para: " + novoStatus);
        carregar();
    }

    private void excluirLeitor() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um leitor para excluir!");
            return;
        }
        int id = (int) tabela.getValueAt(linha, 0);
        String nome = (String) tabela.getValueAt(linha, 1);
        
        int confirmacao = JOptionPane.showConfirmDialog(this, 
            "Tem certeza que deseja excluir " + nome + "?\nEssa ação é irreversível.", 
            "Excluir", JOptionPane.YES_NO_OPTION);
            
        if(confirmacao == JOptionPane.YES_OPTION) {
            try {
                leitorDAO.deletar(id);
                JOptionPane.showMessageDialog(this, "Leitor excluído com sucesso!");
                carregar();
            } catch(Exception e) {
                JOptionPane.showMessageDialog(this, "Erro: Este leitor possui histórico de empréstimos e não pode ser apagado.");
            }
        }
    }

    public void carregar() {
        modeloTabela.setRowCount(0);
        List<Leitor> lista = leitorDAO.listarTodos();
        for (Leitor l : lista) {
            modeloTabela.addRow(new Object[]{l.getId(), l.getNome(), l.getCpf(), l.getEmail(), l.getStatus()});
        }
    }
    
    // Método auxiliar para impedir números no nome
    private void bloquearNumeros(JTextField campo) {
        campo.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // Se for dígito numérico, consome (ignora)
                if (Character.isDigit(c)) {
                    e.consume();
                }
            }
        });
    }
}