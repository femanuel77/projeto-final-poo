package br.com.sgb.view;

import br.com.sgb.dao.EmprestimoDAO;
import br.com.sgb.dao.ExemplarDAO;
import br.com.sgb.dao.LeitorDAO;
import br.com.sgb.model.Emprestimo;
import br.com.sgb.model.Exemplar;
import br.com.sgb.model.Leitor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PainelEmprestimo extends JPanel {
    private JComboBox<Leitor> cbLeitores;
    private JComboBox<Exemplar> cbExemplares; // Vamos mostrar Exemplares disponíveis
    private JTextField txtDataDevolucao;
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    
    private EmprestimoDAO emprestimoDAO;
    private ExemplarDAO exemplarDAO;
    private LeitorDAO leitorDAO;

    public PainelEmprestimo() {
        emprestimoDAO = new EmprestimoDAO();
        exemplarDAO = new ExemplarDAO();
        leitorDAO = new LeitorDAO();
        
        setLayout(new BorderLayout());

        // --- Painel Superior: Novo Empréstimo ---
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Novo Empréstimo"));

        cbLeitores = new JComboBox<>();
        cbExemplares = new JComboBox<>();
        
        // Data automática (Hoje + 7 dias)
        LocalDate dataPrevista = LocalDate.now().plusDays(7);
        txtDataDevolucao = new JTextField(dataPrevista.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        formPanel.add(new JLabel("Selecione o Leitor:"));
        formPanel.add(cbLeitores);
        
        formPanel.add(new JLabel("Livro Disponível (ID - Código):"));
        formPanel.add(cbExemplares);
        
        formPanel.add(new JLabel("Data Prevista Devolução:"));
        formPanel.add(txtDataDevolucao);

        JButton btnEmprestar = new JButton("Registrar Empréstimo");
        btnEmprestar.setBackground(new Color(100, 200, 100)); // Verdezinho
        btnEmprestar.addActionListener(e -> realizarEmprestimo());
        
        formPanel.add(new JLabel("")); // Espaço vazio
        formPanel.add(btnEmprestar);

        add(formPanel, BorderLayout.NORTH);

        // --- Centro: Tabela de Empréstimos Ativos ---
        modeloTabela = new DefaultTableModel(new Object[]{"ID", "Leitor", "Livro", "Data Prevista", "Status"}, 0);
        tabela = new JTable(modeloTabela);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // --- Baixo: Botão de Devolução ---
        JPanel panelBaixo = new JPanel();
        JButton btnDevolver = new JButton("Confirmar Devolução do Item Selecionado");
        btnDevolver.setBackground(new Color(200, 100, 100)); // Vermelhinho
        btnDevolver.setForeground(Color.WHITE);
        btnDevolver.addActionListener(e -> realizarDevolucao());
        
        JButton btnAtualizar = new JButton("🔄 Atualizar Listas");
        btnAtualizar.addActionListener(e -> carregarDados());

        panelBaixo.add(btnDevolver);
        panelBaixo.add(btnAtualizar);
        add(panelBaixo, BorderLayout.SOUTH);

        carregarDados();
    }

    private void carregarDados() {
        // 1. Carregar ComboBox de Leitores
        cbLeitores.removeAllItems();
        List<Leitor> leitores = leitorDAO.listarTodos();
        for (Leitor l : leitores) {
            cbLeitores.addItem(l); // O toString() da classe Leitor vai mostrar o nome
        }

        // 2. Carregar ComboBox de Exemplares (Só os DISPONIVEIS)
        // Nota: Para facilitar o Vibe Coding, vamos listar todos e filtrar na memória ou 
        // idealmente o DAO teria um método 'listarDisponiveis'. Vamos improvisar:
        cbExemplares.removeAllItems();
        // Listamos todos os exemplares de um livro específico? 
        // Não, aqui vamos listar TUDO que tem no banco para ser rápido.
        // OBS: Você precisa cadastrar exemplares na aba Livros primeiro!
        
        // Truque rápido: Vamos pegar todos os livros, e para cada livro, pegar seus exemplares
        // Isso não é performático para milhões de livros, mas para o trabalho serve.
        var livros = new br.com.sgb.dao.LivroDAO().listarTodos();
        for (var livro : livros) {
            var exemplares = exemplarDAO.listarPorLivro(livro.getId());
            for (Exemplar ex : exemplares) {
                if ("DISPONIVEL".equalsIgnoreCase(ex.getStatus())) {
                    // Adicionamos uma string personalizada ou criamos um Wrapper
                    // Vamos usar o próprio objeto Exemplar, mas precisamos sobrescrever toString no model Exemplar
                    // Se não tiver toString lá, vai ficar feio. 
                    // Dica: Adicione toString na classe Exemplar para retornar "Código - Status"
                   cbExemplares.addItem(ex); 
                }
            }
        }

        // 3. Preencher Tabela
        modeloTabela.setRowCount(0);
        List<Emprestimo> emprestimos = emprestimoDAO.listarTodos();
        for (Emprestimo emp : emprestimos) {
            if ("EM_ANDAMENTO".equals(emp.getStatus())) {
                modeloTabela.addRow(new Object[]{
                    emp.getId(),
                    emp.getNomeLeitor(),
                    emp.getTituloLivro(),
                    emp.getDataPrevistaDevolucao(),
                    emp.getStatus()
                });
            }
        }
    }

    private void realizarEmprestimo() {
        try {
            Leitor leitor = (Leitor) cbLeitores.getSelectedItem();
            Exemplar exemplar = (Exemplar) cbExemplares.getSelectedItem();

            if (leitor == null || exemplar == null) {
                JOptionPane.showMessageDialog(this, "Selecione Leitor e Exemplar!");
                return;
            }

            Emprestimo emp = new Emprestimo();
            emp.setLeitorId(leitor.getId());
            emp.setExemplarId(exemplar.getId());
            emp.setDataEmprestimo(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            emp.setDataPrevistaDevolucao(txtDataDevolucao.getText());
            
            emprestimoDAO.registrarEmprestimo(emp);
            
            JOptionPane.showMessageDialog(this, "Empréstimo realizado!");
            carregarDados(); // Atualiza combos e tabela
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void realizarDevolucao() {
        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um empréstimo na tabela para devolver.");
            return;
        }

        int idEmprestimo = (int) tabela.getValueAt(linhaSelecionada, 0);
        
        // Precisamos saber qual exemplar é para liberar ele.
        // No modelo da tabela simplificado não coloquei o ID do exemplar visível.
        // Para resolver rápido: Vamos buscar o empréstimo no banco ou...
        // ...Recuperar da lista original.
        
        // Forma rápida e segura: Pergunta a data de devolução
        String dataHoje = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
        // Aqui temos um pequeno problema lógico: O DAO pede o ID do exemplar para liberar.
        // Vamos ter que buscar esse empréstimo pelo ID para saber qual exemplar é.
        // (Vibe Coding: Vou fazer uma busca rápida na lista da memória)
        
        List<Emprestimo> lista = emprestimoDAO.listarTodos();
        int idExemplar = -1;
        for(Emprestimo e : lista) {
            if(e.getId() == idEmprestimo) {
                idExemplar = e.getExemplarId();
                break;
            }
        }

        if(idExemplar != -1) {
            emprestimoDAO.registrarDevolucao(idEmprestimo, dataHoje, idExemplar);
            JOptionPane.showMessageDialog(this, "Devolução confirmada! Livro disponível novamente.");
            carregarDados();
        }
    }
}