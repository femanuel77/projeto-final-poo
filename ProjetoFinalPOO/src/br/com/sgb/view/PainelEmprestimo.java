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
    // Atenção: Mudamos para guardar um Item wrapper que facilita a busca
    private JComboBox<ItemExemplar> cbExemplares; 
    private JTextField txtDataDevolucao;
    private JTextField txtPesquisaHistorico; 
    private JTextField txtFiltroLivro; 
    private JCheckBox chkVerHistorico; 
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    
    private EmprestimoDAO emprestimoDAO;
    private ExemplarDAO exemplarDAO;
    private LeitorDAO leitorDAO;
    
    private List<Emprestimo> listaEmprestimosCache;
    private java.util.List<ItemExemplar> listaExemplaresCache; 

    private class ItemExemplar {
        Exemplar exemplar;
        String tituloLivro;
        
        public ItemExemplar(Exemplar ex, String titulo) {
            this.exemplar = ex;
            this.tituloLivro = titulo;
        }
        
        @Override
        public String toString() {
            return tituloLivro + " (Cód: " + exemplar.getCodigoBarra() + ")";
        }
    }

    public PainelEmprestimo() {
        emprestimoDAO = new EmprestimoDAO();
        exemplarDAO = new ExemplarDAO();
        leitorDAO = new LeitorDAO();
        
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Novo Empréstimo"));

        cbLeitores = new JComboBox<>();
        
        JPanel panelFiltroLivro = new JPanel(new BorderLayout());
        txtFiltroLivro = new JTextField();
        JButton btnFiltrarExemplar = new JButton("🔎");
        btnFiltrarExemplar.addActionListener(e -> filtrarComboExemplares());
        panelFiltroLivro.add(txtFiltroLivro, BorderLayout.CENTER);
        panelFiltroLivro.add(btnFiltrarExemplar, BorderLayout.EAST);
        
        cbExemplares = new JComboBox<>();
        
        LocalDate dataPrevista = LocalDate.now().plusDays(7);
        txtDataDevolucao = new JTextField(dataPrevista.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        formPanel.add(new JLabel("Selecione o Leitor:")); formPanel.add(cbLeitores);
        formPanel.add(new JLabel("Buscar Livro (Nome/ISBN):")); formPanel.add(panelFiltroLivro);
        formPanel.add(new JLabel("Selecione o Exemplar:")); formPanel.add(cbExemplares);
        formPanel.add(new JLabel("Data Prevista Devolução:")); formPanel.add(txtDataDevolucao);

        JButton btnEmprestar = new JButton("Registrar Empréstimo");
        btnEmprestar.setBackground(new Color(100, 200, 100));
        btnEmprestar.addActionListener(e -> realizarEmprestimo());
        
        formPanel.add(new JLabel("")); formPanel.add(btnEmprestar);
        add(formPanel, BorderLayout.NORTH);

        JPanel centroPanel = new JPanel(new BorderLayout());
        JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtPesquisaHistorico = new JTextField(20);
        JButton btnBuscarHist = new JButton("Buscar na Tabela");
        btnBuscarHist.addActionListener(e -> filtrarTabela());
        
        chkVerHistorico = new JCheckBox("Ver Histórico Completo");
        chkVerHistorico.addActionListener(e -> filtrarTabela());
        
        toolsPanel.add(new JLabel("Pesquisar:")); toolsPanel.add(txtPesquisaHistorico);
        toolsPanel.add(btnBuscarHist); toolsPanel.add(chkVerHistorico);
        centroPanel.add(toolsPanel, BorderLayout.NORTH);

        modeloTabela = new DefaultTableModel(new Object[]{"ID", "Leitor", "Livro", "Previsto", "Devolvido em", "Status"}, 0);
        tabela = new JTable(modeloTabela);
        centroPanel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(centroPanel, BorderLayout.CENTER);

        JPanel panelBaixo = new JPanel();
        JButton btnDevolver = new JButton("Confirmar Devolução");
        btnDevolver.setBackground(new Color(200, 100, 100));
        btnDevolver.setForeground(Color.WHITE);
        btnDevolver.addActionListener(e -> realizarDevolucao());
        
        JButton btnRelatorio = new JButton("📊 Relatório");
        btnRelatorio.addActionListener(e -> gerarRelatorio());
        
        JButton btnAtualizar = new JButton("🔄 Atualizar Listas");
        btnAtualizar.addActionListener(e -> carregarDados());

        panelBaixo.add(btnDevolver); panelBaixo.add(btnRelatorio); panelBaixo.add(btnAtualizar);
        add(panelBaixo, BorderLayout.SOUTH);

        carregarDados();
    }

    public void carregarDados() {
        cbLeitores.removeAllItems();
        List<Leitor> leitores = leitorDAO.listarTodos();
        for (Leitor l : leitores) {
            if("ATIVO".equals(l.getStatus())) cbLeitores.addItem(l);
        }

        cbExemplares.removeAllItems();
        listaExemplaresCache = new java.util.ArrayList<>();
        
        var livros = new br.com.sgb.dao.LivroDAO().listarTodos();
        for (var livro : livros) {
            var exemplares = exemplarDAO.listarPorLivro(livro.getId());
            for (Exemplar ex : exemplares) {
                if ("DISPONIVEL".equalsIgnoreCase(ex.getStatus())) {
                    ItemExemplar item = new ItemExemplar(ex, livro.getTitulo());
                    listaExemplaresCache.add(item);
                    cbExemplares.addItem(item);
                }
            }
        }

        listaEmprestimosCache = emprestimoDAO.listarTodos();
        filtrarTabela();
    }
    
    private void filtrarComboExemplares() {
        String termo = txtFiltroLivro.getText().toLowerCase(); 
        cbExemplares.removeAllItems();
        
        if(listaExemplaresCache == null) return;

        for(ItemExemplar item : listaExemplaresCache) {
            if(item.tituloLivro.toLowerCase().contains(termo) || 
               item.exemplar.getCodigoBarra().toLowerCase().contains(termo)) {
                cbExemplares.addItem(item);
            }
        }
        if(cbExemplares.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "Nenhum livro disponível com esse termo.");
            // Restaura a lista completa
            for(ItemExemplar item : listaExemplaresCache) cbExemplares.addItem(item);
        }
    }

    private void filtrarTabela() {
        if(listaEmprestimosCache == null) return;
        modeloTabela.setRowCount(0);
        String termo = txtPesquisaHistorico.getText().toLowerCase();
        boolean mostrarHistorico = chkVerHistorico.isSelected();

        for (Emprestimo emp : listaEmprestimosCache) {
            boolean matchTexto = emp.getNomeLeitor().toLowerCase().contains(termo) ||
                                 emp.getTituloLivro().toLowerCase().contains(termo);
            
            boolean matchStatus = mostrarHistorico || "EM_ANDAMENTO".equals(emp.getStatus());

            if (matchTexto && matchStatus) {
                modeloTabela.addRow(new Object[]{
                    emp.getId(), emp.getNomeLeitor(), emp.getTituloLivro(),
                    emp.getDataPrevistaDevolucao(),
                    emp.getDataDevolucao() == null ? "-" : emp.getDataDevolucao(),
                    emp.getStatus()
                });
            }
        }
    }

    private void realizarEmprestimo() {
        try {
            Leitor leitor = (Leitor) cbLeitores.getSelectedItem();
            ItemExemplar item = (ItemExemplar) cbExemplares.getSelectedItem(); 

            if (leitor == null || item == null) {
                JOptionPane.showMessageDialog(this, "Selecione Leitor e Exemplar!");
                return;
            }

            Emprestimo emp = new Emprestimo();
            emp.setLeitorId(leitor.getId());
            emp.setExemplarId(item.exemplar.getId()); 
            emp.setDataEmprestimo(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            emp.setDataPrevistaDevolucao(txtDataDevolucao.getText());
            
            emprestimoDAO.registrarEmprestimo(emp);
            JOptionPane.showMessageDialog(this, "Empréstimo realizado!");
            carregarDados(); 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void realizarDevolucao() {
        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um empréstimo!");
            return;
        }

        String status = (String) tabela.getValueAt(linhaSelecionada, 5);
        if(!"EM_ANDAMENTO".equals(status)) {
            JOptionPane.showMessageDialog(this, "Item já devolvido!");
            return;
        }

        int idEmprestimo = (int) tabela.getValueAt(linhaSelecionada, 0);
        String dataHoje = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        
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
            JOptionPane.showMessageDialog(this, "Devolução confirmada!");
            carregarDados();
        }
    }
    
    private void gerarRelatorio() {
        List<Emprestimo> todos = emprestimoDAO.listarTodos();
        long totalEmprestados = todos.stream().filter(e -> "EM_ANDAMENTO".equals(e.getStatus())).count();
        long totalDevolvidos = todos.stream().filter(e -> "FINALIZADO".equals(e.getStatus())).count();
        
        String msg = """
            === RELATÓRIO GERAL ===
            📚 Empréstimos Ativos: %d
            ✅ Devoluções Realizadas: %d
            """.formatted(totalEmprestados, totalDevolvidos);
        JOptionPane.showMessageDialog(this, msg);
    }
}