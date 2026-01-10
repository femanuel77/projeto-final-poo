package br.com.sgb.view;

import javax.swing.*;

public class TelaPrincipal extends JFrame {

    public TelaPrincipal() {
        setTitle("SGB - Sistema de Gerenciamento de Biblioteca (Vibe Coding Edition)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza na tela

        JTabbedPane abas = new JTabbedPane();
        
        // Adiciona os painéis que criamos
        abas.addTab("Gerenciar Livros", new PainelLivro());
        abas.addTab("Gerenciar Leitores", new PainelLeitor());
        abas.addTab("Empréstimos", new PainelEmprestimo());

        add(abas);
    }

    // Método MAIN: Onde tudo começa
    public static void main(String[] args) {
        // Look and Feel do sistema operacional (pra ficar bonito)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new TelaPrincipal().setVisible(true);
        });
    }
}