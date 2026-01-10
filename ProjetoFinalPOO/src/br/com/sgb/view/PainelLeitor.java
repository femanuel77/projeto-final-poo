package br.com.sgb.view;

import br.com.sgb.dao.LeitorDAO;
import br.com.sgb.model.Leitor;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PainelLeitor extends JPanel {
    private JTextField txtCpf, txtNome, txtEmail;
    private JComboBox<String> cbStatus;
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private LeitorDAO leitorDAO;

    public PainelLeitor() {
        leitorDAO = new LeitorDAO();
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 2));
        formPanel.setBorder(BorderFactory.createTitledBorder("Gerenciar Leitor"));

        txtCpf = new JTextField();
        txtNome = new JTextField();
        txtEmail = new JTextField();
        cbStatus = new JComboBox<>(new String[]{"ATIVO", "BLOQUEADO"});

        formPanel.add(new JLabel("CPF:")); formPanel.add(txtCpf);
        formPanel.add(new JLabel("Nome:")); formPanel.add(txtNome);
        formPanel.add(new JLabel("Email:")); formPanel.add(txtEmail);
        formPanel.add(new JLabel("Status:")); formPanel.add(cbStatus);

        JButton btnSalvar = new JButton("Cadastrar Leitor");
        btnSalvar.addActionListener(e -> salvar());

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(formPanel, BorderLayout.CENTER);
        topo.add(btnSalvar, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        modeloTabela = new DefaultTableModel(new Object[]{"ID", "Nome", "CPF", "Status"}, 0);
        tabela = new JTable(modeloTabela);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        
        JButton btnRefresh = new JButton("Atualizar Lista");
        btnRefresh.addActionListener(e -> carregar());
        add(btnRefresh, BorderLayout.SOUTH);

        carregar();
    }

    private void salvar() {
        try {
            Leitor l = new Leitor(txtCpf.getText(), txtNome.getText(), txtEmail.getText(), (String) cbStatus.getSelectedItem());
            leitorDAO.salvar(l);
            JOptionPane.showMessageDialog(this, "Leitor cadastrado!");
            txtCpf.setText(""); txtNome.setText(""); txtEmail.setText("");
            carregar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void carregar() {
        modeloTabela.setRowCount(0);
        List<Leitor> lista = leitorDAO.listarTodos();
        for (Leitor l : lista) {
            modeloTabela.addRow(new Object[]{l.getId(), l.getNome(), l.getCpf(), l.getStatus()});
        }
    }
}