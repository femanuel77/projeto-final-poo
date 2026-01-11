package br.com.sgb.view;

import javax.swing.*;

public class TelaPrincipal extends JFrame {

    public TelaPrincipal() {
        setTitle("SGB - Sistema de Gerenciamento de Biblioteca (Vibe Coding Edition)");
        setSize(900, 650); // Aumentei um pouco pra caber tudo
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

        JTabbedPane abas = new JTabbedPane();
        
        // Criamos os painéis
        PainelLivro painelLivro = new PainelLivro();
        PainelLeitor painelLeitor = new PainelLeitor();
        PainelEmprestimo painelEmprestimo = new PainelEmprestimo();

        abas.addTab("Gerenciar Livros", painelLivro);
        abas.addTab("Gerenciar Leitores", painelLeitor);
        abas.addTab("Empréstimos & Devoluções", painelEmprestimo);

        // --- O TRUQUE DE MESTRE (Atualização Automática) ---
        // Adicionamos um "espião" que vigia quando você muda de aba
        abas.addChangeListener(e -> {
            // Pega o componente que está sendo exibido agora
            java.awt.Component abaAtual = abas.getSelectedComponent();

            if (abaAtual == painelEmprestimo) {
                // Se clicou na aba de empréstimo, recarrega os combos e a tabela
                painelEmprestimo.carregarDados();
            } 
            else if (abaAtual == painelLeitor) {
                // Se quiser recarregar leitor também (opcional)
                // ((PainelLeitor) abaAtual).carregar(); // Precisaria mudar o método lá pra public
            }
            // Não precisa recarregar Livro porque é lá que a gente edita, 
            // mas se quiser, pode fazer o mesmo.
        });

        add(abas);
    }

    public static void main(String[] args) {
        try {
            // Tenta deixar com a cara do Windows/Mac (Nativo)
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) { // Nimbus é um tema moderno do Java
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