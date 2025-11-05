package br.com.neopark.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // Import adicionado
import java.util.Objects;

@Entity
@Table(name = "veiculos")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10, unique = true)
    private String placa;

    private String tipo;
    private String modelo;
    private String cor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mensalista_id")
    private Mensalista mensalista;

    @Column(name = "data_entrada", nullable = false)
    private LocalDateTime dataEntrada;

    protected Veiculo() {}

    public Veiculo(String placa, String tipo, String modelo, String cor, Mensalista mensalista) {
        this.placa = placa;
        this.tipo = tipo;
        this.modelo = modelo;
        this.cor = cor;
        this.mensalista = mensalista;
        this.dataEntrada = LocalDateTime.now(); // Define a data de entrada automaticamente
    }

    public Veiculo(Long id, String placa, String tipo, String modelo, String cor, Mensalista mensalista, LocalDateTime dataEntrada) {
        this.id = id;
        this.placa = placa;
        this.tipo = tipo;
        this.modelo = modelo;
        this.cor = cor;
        this.mensalista = mensalista;
        this.dataEntrada = dataEntrada;
    }

    // Getters e Setters continuam aqui...
    public Long getId() { return id; }
    public String getPlaca() { return placa; }
    public String getTipo() { return tipo; }
    public String getModelo() { return modelo; }
    public String getCor() { return cor; }
    public Mensalista getMensalista() {return mensalista;}
    public LocalDateTime getDataEntrada() { return dataEntrada; }

    public void setId(Long id) { this.id = id; }
    public void setPlaca(String placa) { this.placa = placa; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public void setCor(String cor) { this.cor = cor; }
    public void setMensalista(Mensalista mensalista) {this.mensalista = mensalista;}
    public void setDataEntrada(LocalDateTime dataEntrada) { this.dataEntrada = dataEntrada; }


    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Veiculo v)) return false;
        return Objects.equals(id, v.id);
    }
    @Override public int hashCode() { return Objects.hash(id); }

    /**
     * Formata a saída do objeto Veiculo de forma mais legível.
     * Exemplo: [ Veículo | ID: 1 | Placa: PTX5577 | Carro Virtus Branco | Mensalista: Não | Entrada: 29/09/2025 14:56:06 ]
     */
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dataFormatada = (this.dataEntrada != null) ? this.dataEntrada.format(formatter) : "N/A";
        String statusMensalista;
        if (this.mensalista != null){
            statusMensalista = "Sim (" + this.mensalista.getNome() + ")";
        } else {
        statusMensalista = "Não";
        }
    

        return String.format(
                "[ Veículo | ID: %d | Placa: %s | %s %s %s | Mensalista: %s | Entrada: %s ]",
                id,
                placa,
                tipo,
                modelo,
                cor,
                statusMensalista,
                dataFormatada
        );
    }
}