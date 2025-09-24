package entities;

public class Condutor {
    
    private String nome;
    private String cpf;
    private String telefone;
    private Veiculo veiculo;

    public Condutor(String nome, String cpf, String telefone, Veiculo veiculo) {
        
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.veiculo = veiculo;
    }

    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getTelefone() { return telefone; }
    public Veiculo getVeiculo() { return veiculo; }

    public void setNome(String nome) { this.nome = nome; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setVeiculo(Veiculo veiculo) { this.veiculo = veiculo; }

    @Override
    public String toString() {
        return "\nNome: " + nome + "\nCPF: " + cpf + "\nTelefone: " + telefone + (veiculo != null ? "\nVeiculo: " + veiculo : "\nVeiculo não cadastrado");
    }
    

}
