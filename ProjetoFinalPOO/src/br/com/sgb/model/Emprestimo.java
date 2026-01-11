package br.com.sgb.model;

public class Emprestimo {
    private int id;
    private String dataEmprestimo;
    private String dataPrevistaDevolucao;
    private String dataDevolucao;
    private String status; 
    
    // Relacionamentos
    private int leitorId;
    private int exemplarId;

    private String nomeLeitor;
    private String tituloLivro;

    public Emprestimo() {}

    public Emprestimo(String dataEmprestimo, String dataPrevista, int leitorId, int exemplarId) {
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevistaDevolucao = dataPrevista;
        this.status = "EM_ANDAMENTO";
        this.leitorId = leitorId;
        this.exemplarId = exemplarId;
    }

    // Getters e Setters padrões
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDataEmprestimo() { return dataEmprestimo; }
    public void setDataEmprestimo(String dataEmprestimo) { this.dataEmprestimo = dataEmprestimo; }

    public String getDataPrevistaDevolucao() { return dataPrevistaDevolucao; }
    public void setDataPrevistaDevolucao(String dataPrevistaDevolucao) { this.dataPrevistaDevolucao = dataPrevistaDevolucao; }

    public String getDataDevolucao() { return dataDevolucao; }
    public void setDataDevolucao(String dataDevolucao) { this.dataDevolucao = dataDevolucao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getLeitorId() { return leitorId; }
    public void setLeitorId(int leitorId) { this.leitorId = leitorId; }

    public int getExemplarId() { return exemplarId; }
    public void setExemplarId(int exemplarId) { this.exemplarId = exemplarId; }
    
    // Getters e Setters auxiliares
    public String getNomeLeitor() { return nomeLeitor; }
    public void setNomeLeitor(String nomeLeitor) { this.nomeLeitor = nomeLeitor; }
    public String getTituloLivro() { return tituloLivro; }
    public void setTituloLivro(String tituloLivro) { this.tituloLivro = tituloLivro; }
}