package br.com.sgb.model;

public class Exemplar {
    private int id;
    private String codigoBarra;
    private String status; 
    private int livroId;   

    public Exemplar() {}

    public Exemplar(String codigoBarra, String status, int livroId) {
        this.codigoBarra = codigoBarra;
        this.status = status;
        this.livroId = livroId;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigoBarra() { return codigoBarra; }
    public void setCodigoBarra(String codigoBarra) { this.codigoBarra = codigoBarra; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getLivroId() { return livroId; }
    public void setLivroId(int livroId) { this.livroId = livroId; }

    @Override
    public String toString() {
        return "Cód: " + this.codigoBarra + " (ID Livro: " + this.livroId + ")";
    }
}