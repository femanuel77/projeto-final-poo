package br.com.sgb.view;

import br.com.sgb.dao.LeitorDAO;
import br.com.sgb.model.Leitor;
import javax.swing.*;
import javax.swing.text.MaskFormatter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

public class PainelLeitor extends JPanel {
    private JFormattedTextField txtCpf;
    private JTextField txtNome, txtEmail;
    private JComboBox<String> cbStatus;
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private LeitorDAO leitorDAO;
    

    private int idEmEdicao = -1; 
    private JButton btnSalvar;

    public PainelLeitor() {
        leitorDAO = new LeitorDAO();
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastrar / Editar Leitor"));

        try {
            MaskFormatter mascaraCpf = new MaskFormatter("###.###.###-##");
            mascaraCpf.setPlaceholderCharacter('_');
            txtCpf = new JFormattedTextField(mascaraCpf);
        } catch (Exception e) { txtCpf = new JFormattedTextField(); }

        txtNome = new JTextField();
        bloquearNumeros(txtNome);
        txtEmail = new JTextField();
        cbStatus = new JComboBox<>(new String[]{"ATIVO", "BLOQUEADO"});

        formPanel.add(new JLabel("CPF * :")); formPanel.add(txtCpf);
        formPanel.add(new JLabel("Nome * :")); formPanel.add(txtNome);
        formPanel.add(new JLabel("Email * :")); formPanel.add(txtEmail);
        formPanel.add(new JLabel("Status:")); formPanel.add(cbStatus);

        btnSalvar = new JButton("Cadastrar Leitor");
        btnSalvar.addActionListener(e -> salvarOuAtualizar());
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> resetarFormulario());
        
        JPanel painelBotoesForm = new JPanel();
        painelBotoesForm.add(btnSalvar); painelBotoesForm.add(btnCancelar);

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(formPanel, BorderLayout.CENTER);
        topo.add(painelBotoesForm, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        modeloTabela = new DefaultTableModel(new Object[]{"ID", "Nome", "CPF", "Email", "Status"}, 0);
        tabela = new JTable(modeloTabela);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        
        JPanel rodape = new JPanel();
        JButton btnEditar = new JButton("✏️ Editar");
        btnEditar.setBackground(new Color(255, 255, 200));
        btnEditar.addActionListener(e -> carregarParaEdicao());
        
        JButton btnStatus = new JButton("Bloquear/Desbloquear");
        btnStatus.addActionListener(e -> alternarStatus());
        
        JButton btnExcluir = new JButton("🗑️ Excluir");
        btnExcluir.setBackground(new Color(255, 100, 100));
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.addActionListener(e -> excluirLeitor());
        
        JButton btnRefresh = new JButton("Atualizar");
        btnRefresh.addActionListener(e -> carregar());
        
        rodape.add(btnEditar); rodape.add(btnStatus); rodape.add(btnExcluir); rodape.add(btnRefresh);
        add(rodape, BorderLayout.SOUTH);

        carregar();
    }
    
    private void carregarParaEdicao() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um leitor para editar.");
            return;
        }
        idEmEdicao = (int) tabela.getValueAt(linha, 0);
        txtNome.setText((String) tabela.getValueAt(linha, 1));
        txtCpf.setValue((String) tabela.getValueAt(linha, 2));
        txtEmail.setText((String) tabela.getValueAt(linha, 3));
        cbStatus.setSelectedItem((String) tabela.getValueAt(linha, 4));
        
        btnSalvar.setText("💾 Salvar Alterações");
        btnSalvar.setBackground(new Color(255, 200, 100));
    }

    private void salvarOuAtualizar() {
        try {
            String cpfOriginal = (String) txtCpf.getValue();
            if (cpfOriginal == null || cpfOriginal.contains("_")) {
                JOptionPane.showMessageDialog(this, "CPF inválido!"); return;
            }
            if (txtNome.getText().trim().isEmpty() || txtEmail.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome e Email obrigatórios!"); return;
            }
            String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
            if (!Pattern.matches(emailRegex, txtEmail.getText())) {
                JOptionPane.showMessageDialog(this, "E-mail inválido!"); return;
            }

            Leitor l = new Leitor(cpfOriginal, txtNome.getText(), txtEmail.getText(), (String) cbStatus.getSelectedItem());

            if (idEmEdicao == -1) {
                // INSERT
                leitorDAO.salvar(l);
                JOptionPane.showMessageDialog(this, "Leitor cadastrado!");
            } else {
                // UPDATE
                l.setId(idEmEdicao);
                leitorDAO.atualizar(l);
                JOptionPane.showMessageDialog(this, "Leitor atualizado!");
            }
            
            resetarFormulario();
            carregar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }
    
    private void resetarFormulario() {
        idEmEdicao = -1;
        txtCpf.setValue(null); txtNome.setText(""); txtEmail.setText("");
        btnSalvar.setText("Cadastrar Leitor");
        btnSalvar.setBackground(null);
    }

    private void alternarStatus() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) return;
        int id = (int) tabela.getValueAt(linha, 0);
        String novo = tabela.getValueAt(linha, 4).equals("ATIVO") ? "BLOQUEADO" : "ATIVO";
        leitorDAO.atualizarStatus(id, novo);
        carregar();
    }

    private void excluirLeitor() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) return;
        int id = (int) tabela.getValueAt(linha, 0);
        if (JOptionPane.showConfirmDialog(this, "Excluir leitor?", "Confirmação", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { leitorDAO.deletar(id); carregar(); } catch(Exception e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
        }
    }

    public void carregar() {
        modeloTabela.setRowCount(0);
        for (Leitor l : leitorDAO.listarTodos()) {
            modeloTabela.addRow(new Object[]{l.getId(), l.getNome(), l.getCpf(), l.getEmail(), l.getStatus()});
        }
    }
    
    private void bloquearNumeros(JTextField campo) {
        campo.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (Character.isDigit(e.getKeyChar())) e.consume();
            }
        });
    }
}