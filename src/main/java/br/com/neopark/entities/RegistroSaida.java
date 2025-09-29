package br.com.neopark.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros_saida")
public class RegistroSaida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String placa;

    @Column(nullable = false)
    private LocalDateTime entrada;

    @Column(nullable = false)
    private LocalDateTime saida;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPago;

    protected RegistroSaida() {}

    public RegistroSaida(String placa, LocalDateTime entrada, LocalDateTime saida, BigDecimal valorPago) {
        this.placa = placa;
        this.entrada = entrada;
        this.saida = saida;
        this.valorPago = valorPago;
    }

    public Long getId() { return id; }
    public String getPlaca() { return placa; }
    public LocalDateTime getEntrada() { return entrada; }
    public LocalDateTime getSaida() { return saida; }
    public BigDecimal getValorPago() { return valorPago; }
}
