package br.com.sgb.view;

import javax.swing.*;

public class TelaPrincipal extends JFrame {

    public TelaPrincipal() {
        setTitle("SGB - Sistema de Gerenciamento de Biblioteca (Vibe Coding Edition)");
        setSize(900, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

        JTabbedPane abas = new JTabbedPane();
        
        // Painéis
        PainelLivro painelLivro = new PainelLivro();
        PainelLeitor painelLeitor = new PainelLeitor();
        PainelEmprestimo painelEmprestimo = new PainelEmprestimo();

        abas.addTab("Gerenciar Livros", painelLivro);
        abas.addTab("Gerenciar Leitores", painelLeitor);
        abas.addTab("Empréstimos & Devoluções", painelEmprestimo);

        abas.addChangeListener(e -> {
            java.awt.Component abaAtual = abas.getSelectedComponent();

            if (abaAtual == painelEmprestimo) {
                painelEmprestimo.carregarDados();
            } 
        });

        add(abas);
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) { 
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        SwingUtilities.invokeLater(() -> {
            new TelaPrincipal().setVisible(true);
        });
    }
}