package br.com.sgb.model;

public class Livro {
    private int id;
    private String isbn;
    private String titulo;
    private int edicao;
    private String editora;
    private int anoPublicacao;

    public Livro() {}

    public Livro(String isbn, String titulo, int edicao, String editora, int anoPublicacao) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.edicao = edicao;
        this.editora = editora;
        this.anoPublicacao = anoPublicacao;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public int getEdicao() { return edicao; }
    public void setEdicao(int edicao) { this.edicao = edicao; }

    public String getEditora() { return editora; }
    public void setEditora(String editora) { this.editora = editora; }

    public int getAnoPublicacao() { return anoPublicacao; }
    public void setAnoPublicacao(int anoPublicacao) { this.anoPublicacao = anoPublicacao; }
    
    @Override
    public String toString() {
        return this.titulo + " (" + this.editora + ")"; 
    }
}