package br.com.sgb.model;

public class Leitor {
    private int id;
    private String cpf;
    private String nome;
    private String email;
    private String status; 

    public Leitor() {}

    public Leitor(String cpf, String nome, String email, String status) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.status = status;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return this.nome + " (" + this.cpf + ")";
    }
}