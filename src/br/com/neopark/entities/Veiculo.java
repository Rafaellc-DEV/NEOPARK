package br.com.neopark.entities;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "veiculos")
public class Veiculo {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "placa", nullable = false, lenght = 10, unique = true)
    private String placa;

    @Column(name = "tipo", lenght = 50)
    private String tipo;

     @Column(name = "modelo", lenght = 100)
    private String modelo;

    @Column(name = "cor", lenght = 30)
    private String cor;

    @Column(name = "mensalista", nullable = false)
    private Boolean mensalista;

    @Column(name = "data_entrada", nullable = false)
    private LocalDateTime dataEntrada;

    // construtor vazio para o jpa
    public Veiculo() {}
    

    public Veiculo(Long id, String placa, String tipo, String modelo, String cor, Boolean mensalista, LocalDataTime dataEntrada) {
        this.id = id;
        this.placa = placa;
        this.tipo = tipo;
        this.modelo = modelo;
        this.cor = cor;
        this.mensalista = mensalista;
        this.dataEntrada = dataEntrada;
    }

    public Long getId() { return id; }
    public String getPlaca() { return placa; }
    public String getTipo() { return tipo; }
    public String getModelo() { return modelo; }
    public String getCor() { return cor; }
    public String getmensalista() { return mensalista;}

    public void setId(Long id) {this.id = id;}
    public void setPlaca(String placa) { this.placa = placa; }
    public void setTipo(String  tipo) { this.tipo = tipo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public void setCor(String cor) { this.cor = cor; }
    public void setmensalista(Boolean mensalista) { this.mensalista = mensalista;}

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", placa='" + placa + '\'' +
                ", tipo='" + tipo + '\'' +
                ", modelo='" + modelo + '\'' +
                ", cor='" + cor + '\'' +
                ", mensalista=" + mensalista +
                ", dataEntrada=" + dataEntrada +
                '}';
    }

    @Override

}

