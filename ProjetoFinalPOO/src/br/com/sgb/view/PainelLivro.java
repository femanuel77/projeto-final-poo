package br.com.sgb.view;

import br.com.sgb.dao.ExemplarDAO;
import br.com.sgb.dao.LivroDAO;
import br.com.sgb.model.Exemplar;
import br.com.sgb.model.Livro;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Collectors;

public class PainelLivro extends JPanel {
    private JTextField txtIsbn, txtTitulo, txtEdicao, txtEditora, txtAno, txtQtdExemplares;
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private LivroDAO livroDAO;
    private ExemplarDAO exemplarDAO;
    private List<Livro> listaCache;

    public PainelLivro() {
        this.livroDAO = new LivroDAO();
        this.exemplarDAO = new ExemplarDAO();
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastrar Novo Livro (Obra)"));

        txtIsbn = new JTextField();
        txtTitulo = new JTextField();
        txtEdicao = new JTextField();
        txtEditora = new JTextField();
        txtAno = new JTextField();
        txtQtdExemplares = new JTextField();

        // --- VALIDAÇÃO REAL-TIME ---
        // Bloqueia letras em campos numéricos
        bloquearLetras(txtEdicao);
        bloquearLetras(txtAno);
        bloquearLetras(txtQtdExemplares);
        
        // NOVO: ISBN agora só aceita números
        bloquearLetras(txtIsbn);

        formPanel.add(new JLabel("ISBN (Min 10 núm) * :")); formPanel.add(txtIsbn);
        formPanel.add(new JLabel("Título * :")); formPanel.add(txtTitulo);
        formPanel.add(new JLabel("Edição (nº) * :")); formPanel.add(txtEdicao);
        formPanel.add(new JLabel("Editora * :")); formPanel.add(txtEditora);
        formPanel.add(new JLabel("Ano * :")); formPanel.add(txtAno);
        formPanel.add(new JLabel("Qtd. Exemplares Iniciais * :")); formPanel.add(txtQtdExemplares);

        JButton btnSalvar = new JButton("Salvar Livro e Gerar Exemplares");
        btnSalvar.addActionListener(e -> salvarLivroAutomatico());
        
        JPanel topo = new JPanel(new BorderLayout());
        topo.add(formPanel, BorderLayout.CENTER);
        topo.add(btnSalvar, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        JPanel panelBusca = new JPanel(new BorderLayout());
        JTextField txtPesquisa = new JTextField();
        JButton btnBuscar = new JButton("🔍 Buscar");
        btnBuscar.addActionListener(e -> filtrarTabela(txtPesquisa.getText()));
        
        panelBusca.add(new JLabel("Pesquisar por Título: "), BorderLayout.WEST);
        panelBusca.add(txtPesquisa, BorderLayout.CENTER);
        panelBusca.add(btnBuscar, BorderLayout.EAST);

        modeloTabela = new DefaultTableModel(new Object[]{"ID", "ISBN", "Título", "Editora", "Ano"}, 0);
        tabela = new JTable(modeloTabela);
        
        JPanel centro = new JPanel(new BorderLayout());
        centro.add(panelBusca, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel panelBotoes = new JPanel();
        JButton btnAddExemplar = new JButton("➕ Adicionar Lote de Cópias");
        btnAddExemplar.setBackground(new Color(200, 230, 255));
        btnAddExemplar.addActionListener(e -> adicionarExemplar());

        JButton btnExcluir = new JButton("🗑️ Excluir Livro Selecionado");
        btnExcluir.setBackground(new Color(255, 100, 100));
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.addActionListener(e -> excluirLivro());
        
        JButton btnAtualizar = new JButton("Recarregar Lista");
        btnAtualizar.addActionListener(e -> carregarTabela());

        panelBotoes.add(btnAddExemplar); panelBotoes.add(btnExcluir); panelBotoes.add(btnAtualizar);
        add(panelBotoes, BorderLayout.SOUTH);

        carregarTabela();
    }

    private void salvarLivroAutomatico() {
        try {
            if(txtTitulo.getText().trim().isEmpty() || txtIsbn.getText().trim().isEmpty() || 
               txtEdicao.getText().trim().isEmpty() || txtEditora.getText().trim().isEmpty() ||
               txtAno.getText().trim().isEmpty() || txtQtdExemplares.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos os campos marcados com * são obrigatórios!");
                return;
            }

            String isbnRaw = txtIsbn.getText().replaceAll("[^0-9]", ""); 
            if (isbnRaw.length() < 10) {
                JOptionPane.showMessageDialog(this, "ISBN inválido! Mínimo de 10 números.");
                return;
            }

            Livro l = new Livro();
            l.setIsbn(txtIsbn.getText());
            l.setTitulo(txtTitulo.getText());
            l.setEdicao(Integer.parseInt(txtEdicao.getText()));
            l.setEditora(txtEditora.getText());
            l.setAnoPublicacao(Integer.parseInt(txtAno.getText()));

            livroDAO.salvar(l);
            int idLivroRecuperado = buscarIdPorIsbn(l.getIsbn());
            
            int qtd = Integer.parseInt(txtQtdExemplares.getText());
            for (int i = 1; i <= qtd; i++) {
                String codigoGerado = l.getIsbn() + "-" + i;
                Exemplar ex = new Exemplar(codigoGerado, "DISPONIVEL", idLivroRecuperado);
                exemplarDAO.salvar(ex);
            }

            JOptionPane.showMessageDialog(this, "Livro salvo e " + qtd + " exemplares gerados!");
            txtIsbn.setText(""); txtTitulo.setText(""); txtEdicao.setText(""); 
            txtEditora.setText(""); txtAno.setText(""); txtQtdExemplares.setText("");
            carregarTabela();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }

    private void adicionarExemplar() {
        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um LIVRO na tabela primeiro!");
            return;
        }

        int idLivro = (int) tabela.getValueAt(linhaSelecionada, 0);
        String tituloLivro = (String) tabela.getValueAt(linhaSelecionada, 2);
        String isbnLivro = (String) tabela.getValueAt(linhaSelecionada, 1);

        String entrada = JOptionPane.showInputDialog(this, 
            "Quantas NOVAS cópias adicionar para: " + tituloLivro + "?", "1");

        if (entrada != null) {
            try {
                int qtd = Integer.parseInt(entrada);
                List<Exemplar> existentes = exemplarDAO.listarPorLivro(idLivro);
                int proximoNumero = existentes.size() + 1;

                for (int i = 0; i < qtd; i++) {
                    String codigoGerado = isbnLivro + "-" + (proximoNumero + i);
                    Exemplar ex = new Exemplar(codigoGerado, "DISPONIVEL", idLivro);
                    exemplarDAO.salvar(ex);
                }
                JOptionPane.showMessageDialog(this, qtd + " exemplares adicionados!");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Digite apenas números inteiros!");
            }
        }
    }
    
    private void excluirLivro() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um livro para excluir.");
            return;
        }
        int id = (int) tabela.getValueAt(linha, 0);
        String titulo = (String) tabela.getValueAt(linha, 2);

        int confirmacao = JOptionPane.showConfirmDialog(this, 
            "Tem certeza que deseja excluir '" + titulo + "'?",
            "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                livroDAO.deletar(id);
                JOptionPane.showMessageDialog(this, "Livro excluído com sucesso!");
                carregarTabela();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
            }
        }
    }

    private int buscarIdPorIsbn(String isbn) {
        List<Livro> todos = livroDAO.listarTodos();
        for (Livro l : todos) {
            if (l.getIsbn().equals(isbn)) return l.getId();
        }
        return -1;
    }

    // Método que impede digitar qualquer coisa que não seja número
    private void bloquearLetras(JTextField campo) {
        campo.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // Permite apenas dígitos e teclas de controle (Backspace/Delete)
                if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
                    e.consume();
                }
            }
        });
    }

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        listaCache = livroDAO.listarTodos();
        atualizarGrid(listaCache);
    }

    private void filtrarTabela(String texto) {
        if (listaCache == null) return;
        String termo = texto.toLowerCase();
        List<Livro> filtrados = listaCache.stream()
            .filter(l -> l.getTitulo().toLowerCase().contains(termo) || 
                         l.getEditora().toLowerCase().contains(termo))
            .collect(Collectors.toList());
        atualizarGrid(filtrados);
    }

    private void atualizarGrid(List<Livro> livros) {
        modeloTabela.setRowCount(0);
        for (Livro l : livros) {
            modeloTabela.addRow(new Object[]{l.getId(), l.getIsbn(), l.getTitulo(), l.getEditora(), l.getAnoPublicacao()});
        }
    }
}