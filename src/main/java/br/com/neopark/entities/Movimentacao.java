package br.com.neopark.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_movimentacao") 
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String placa;
    
    @Column(name = "data_entrada")
    private LocalDateTime dataEntrada;
    
    @Column(name = "data_saida")
    private LocalDateTime dataSaida; 
    
    @Column(name = "valor_pago")
    private BigDecimal valorPago;
    
    @Column(name = "is_mensalista")
    private boolean isMensalista;

    public Movimentacao() {
    }

    // CONSTRUTOR para ser usado no EstacionamentoService.registrarSaidaAvulso
    public Movimentacao(String placa, LocalDateTime dataEntrada, LocalDateTime dataSaida, BigDecimal valorPago, boolean isMensalista) {
        this.placa = placa;
        this.dataEntrada = dataEntrada;
        this.dataSaida = dataSaida;
        this.valorPago = valorPago;
        this.isMensalista = isMensalista;
    }

    // Método toString para exibição no menu (História 10)
    @Override
    public String toString() {
        String tipoCliente = isMensalista ? "Mensalista" : "Avulso";
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");

        return String.format("| %-8s | %-12s | %-15s | %-15s | R$ %-7.2f |", 
            placa, 
            tipoCliente, 
            dataEntrada.format(formatter), 
            dataSaida.format(formatter), 
            valorPago);
    }


    // Getters
    public Long getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public LocalDateTime getDataSaida() {
        return dataSaida;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public boolean isMensalista() {
        return isMensalista;
    }
    // Setters omitidos para brevidade.
}