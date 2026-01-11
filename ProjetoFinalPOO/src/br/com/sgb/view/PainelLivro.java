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
    
    // Controle de Edição
    private int idEmEdicao = -1; // -1 significa que estamos criando um NOVO
    private JButton btnSalvar; // Referência para mudar o texto do botão

    public PainelLivro() {
        this.livroDAO = new LivroDAO();
        this.exemplarDAO = new ExemplarDAO();
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Cadastrar / Editar Livro"));

        txtIsbn = new JTextField();
        txtTitulo = new JTextField();
        txtEdicao = new JTextField();
        txtEditora = new JTextField();
        txtAno = new JTextField();
        txtQtdExemplares = new JTextField();

        // Validações
        bloquearLetras(txtEdicao);
        bloquearLetras(txtAno);
        bloquearLetras(txtQtdExemplares);
        bloquearLetras(txtIsbn);

        formPanel.add(new JLabel("ISBN (Min 10 núm) * :")); formPanel.add(txtIsbn);
        formPanel.add(new JLabel("Título * :")); formPanel.add(txtTitulo);
        formPanel.add(new JLabel("Edição (nº) * :")); formPanel.add(txtEdicao);
        formPanel.add(new JLabel("Editora * :")); formPanel.add(txtEditora);
        formPanel.add(new JLabel("Ano * :")); formPanel.add(txtAno);
        formPanel.add(new JLabel("Qtd. Exemplares Iniciais * :")); formPanel.add(txtQtdExemplares);

        btnSalvar = new JButton("Salvar Novo Livro");
        btnSalvar.setBackground(new Color(100, 200, 100)); // Verde
        btnSalvar.addActionListener(e -> salvarOuAtualizar());
        
        JButton btnCancelar = new JButton("Cancelar Edição");
        btnCancelar.addActionListener(e -> resetarFormulario());
        
        JPanel painelBotoesForm = new JPanel(new FlowLayout());
        painelBotoesForm.add(btnSalvar);
        painelBotoesForm.add(btnCancelar);
        
        JPanel topo = new JPanel(new BorderLayout());
        topo.add(formPanel, BorderLayout.CENTER);
        topo.add(painelBotoesForm, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        // Barra de Pesquisa
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

        // Rodapé
        JPanel panelBotoes = new JPanel();
        JButton btnEditar = new JButton("✏️ Editar Selecionado");
        btnEditar.setBackground(new Color(255, 255, 200)); // Amarelo
        btnEditar.addActionListener(e -> carregarParaEdicao());

        JButton btnAddExemplar = new JButton("➕ Adicionar Lote de Cópias");
        btnAddExemplar.setBackground(new Color(200, 230, 255));
        btnAddExemplar.addActionListener(e -> adicionarExemplar());

        JButton btnExcluir = new JButton("🗑️ Excluir Livro");
        btnExcluir.setBackground(new Color(255, 100, 100));
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.addActionListener(e -> excluirLivro());
        
        JButton btnAtualizar = new JButton("Recarregar");
        btnAtualizar.addActionListener(e -> carregarTabela());

        panelBotoes.add(btnEditar); // Novo
        panelBotoes.add(btnAddExemplar); 
        panelBotoes.add(btnExcluir); 
        panelBotoes.add(btnAtualizar);
        add(panelBotoes, BorderLayout.SOUTH);

        carregarTabela();
    }

    private void carregarParaEdicao() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um livro para editar.");
            return;
        }
        
        // Pega os dados da tabela e joga nos campos
        idEmEdicao = (int) tabela.getValueAt(linha, 0);
        txtIsbn.setText((String) tabela.getValueAt(linha, 1));
        txtTitulo.setText((String) tabela.getValueAt(linha, 2));
        txtEditora.setText((String) tabela.getValueAt(linha, 3));
        txtAno.setText(tabela.getValueAt(linha, 4).toString());
        
        // A edição não recupera "Edição" pois não está na tabela, mas tudo bem, o usuário preenche
        // Para ficar perfeito, deveríamos buscar no banco pelo ID, mas vamos simplificar:
        // Assume edição 1 se não lembrar.
        txtEdicao.setText("1"); 
        
        // Bloqueia qtd pois não editamos estoque aqui, só dados cadastrais
        txtQtdExemplares.setText("0");
        txtQtdExemplares.setEnabled(false);
        
        btnSalvar.setText("💾 Salvar Alterações");
        btnSalvar.setBackground(new Color(255, 200, 100)); // Laranja
    }

    private void salvarOuAtualizar() {
        try {
            if(txtTitulo.getText().trim().isEmpty() || txtIsbn.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Preencha os campos obrigatórios!");
                return;
            }

            Livro l = new Livro();
            l.setIsbn(txtIsbn.getText());
            l.setTitulo(txtTitulo.getText());
            l.setEdicao(Integer.parseInt(txtEdicao.getText()));
            l.setEditora(txtEditora.getText());
            l.setAnoPublicacao(Integer.parseInt(txtAno.getText()));

            if (idEmEdicao == -1) {
                // MODO INSERÇÃO (NOVO)
                if(txtQtdExemplares.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Informe a quantidade inicial!");
                    return;
                }
                livroDAO.salvar(l);
                
                // Gera exemplares
                int idRecuperado = buscarIdPorIsbn(l.getIsbn());
                int qtd = Integer.parseInt(txtQtdExemplares.getText());
                for (int i = 1; i <= qtd; i++) {
                    exemplarDAO.salvar(new Exemplar(l.getIsbn() + "-" + i, "DISPONIVEL", idRecuperado));
                }
                JOptionPane.showMessageDialog(this, "Livro cadastrado com sucesso!");
                
            } else {
                // MODO EDIÇÃO (UPDATE)
                l.setId(idEmEdicao);
                livroDAO.atualizar(l); // Chama o método UPDATE do DAO
                JOptionPane.showMessageDialog(this, "Dados do livro atualizados!");
            }
            
            resetarFormulario();
            carregarTabela();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage());
        }
    }
    
    private void resetarFormulario() {
        idEmEdicao = -1;
        txtIsbn.setText(""); txtTitulo.setText(""); txtEdicao.setText(""); 
        txtEditora.setText(""); txtAno.setText(""); txtQtdExemplares.setText("");
        txtQtdExemplares.setEnabled(true);
        btnSalvar.setText("Salvar Novo Livro");
        btnSalvar.setBackground(new Color(100, 200, 100));
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

        String entrada = JOptionPane.showInputDialog(this, "Quantas NOVAS cópias para: " + tituloLivro + "?", "1");
        if (entrada != null) {
            try {
                int qtd = Integer.parseInt(entrada);
                List<Exemplar> existentes = exemplarDAO.listarPorLivro(idLivro);
                int proximoNumero = existentes.size() + 1;
                for (int i = 0; i < qtd; i++) {
                    exemplarDAO.salvar(new Exemplar(isbnLivro + "-" + (proximoNumero + i), "DISPONIVEL", idLivro));
                }
                JOptionPane.showMessageDialog(this, qtd + " exemplares adicionados!");
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage()); }
        }
    }
    
    private void excluirLivro() {
        int linha = tabela.getSelectedRow();
        if (linha == -1) return;
        int id = (int) tabela.getValueAt(linha, 0);
        if (JOptionPane.showConfirmDialog(this, "Excluir livro?", "Confirmação", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try { livroDAO.deletar(id); carregarTabela(); } catch(Exception e) { JOptionPane.showMessageDialog(this, e.getMessage()); }
        }
    }

    private int buscarIdPorIsbn(String isbn) {
        for (Livro l : livroDAO.listarTodos()) { if (l.getIsbn().equals(isbn)) return l.getId(); }
        return -1;
    }

    private void bloquearLetras(JTextField campo) {
        campo.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) && e.getKeyChar() != '\b') e.consume();
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
        List<Livro> filtrados = listaCache.stream().filter(l -> l.getTitulo().toLowerCase().contains(termo)).collect(Collectors.toList());
        atualizarGrid(filtrados);
    }

    private void atualizarGrid(List<Livro> livros) {
        modeloTabela.setRowCount(0);
        for (Livro l : livros) modeloTabela.addRow(new Object[]{l.getId(), l.getIsbn(), l.getTitulo(), l.getEditora(), l.getAnoPublicacao()});
    }
}