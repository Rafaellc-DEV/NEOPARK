package br.com.neopark.entities;
import jakarta.persistence.*;

@Entity
@Table(name = "condutor")
public class Condutor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length =100)
    private String nome;

    @Column(name = "cpf", nullable = false, length = 14, unique = true)
    private String cpf;

    @Column(name = "telefone", length = 20)
    private String telefone;  

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", referencedColumnName = "id")
    private Veiculo veiculo;

    // construtor vazio para o jpa
    public Condutor() {}


    public Condutor(Long Id, String nome, String cpf, String telefone, Veiculo veiculo) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.veiculo = veiculo;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getTelefone() { return telefone; }
    public Veiculo getVeiculo() { return veiculo; }

    public void setId(Long id) { this.id = id;}
    public void setNome(String nome) { this.nome = nome; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setVeiculo(Veiculo veiculo) { this.veiculo = veiculo; }

    @Override
    public String toString() {
        return "\nNome: " + nome + "\nCPF: " + cpf + "\nTelefone: " + telefone + (veiculo != null ? "\nVeiculo: " + veiculo : "\nVeiculo não cadastrado");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Condutor)) return false; 
        Condutor condutor = (Condutor) o;
        return Objects.equals(id, condutor.id);
    }

    @Override
    public int hashCode() { 
        return Objects.hash(id);
    }

}
