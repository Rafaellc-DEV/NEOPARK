package entities;

import entities.Veiculo;

public class Condutor {
    
    private String nome;
    private String cpf;
    private String telefone;
    private Veiculo veiculo;

    public Condutor(String nome, String cpf, String telefone) {
        
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;

    }

    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getTelefone() { return telefone; }

    public void setNome(String nome) { this.nome = nome; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    

}
