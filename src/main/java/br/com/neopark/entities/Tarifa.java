package br.com.neopark.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarifas")
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Valor cobrado por hora (obrigatório, > 0) */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorHora;

    /** Percentual de desconto para mensalista (0–100). Ex.: 20 = 20% */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal descontoMensalista;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    private LocalDateTime atualizadoEm;

    @PreUpdate
    public void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public Tarifa() {}

    public Tarifa(BigDecimal valorHora, BigDecimal descontoMensalista) {
        this.valorHora = valorHora;
        this.descontoMensalista = descontoMensalista;
    }

    public Long getId() { return id; }

    public BigDecimal getValorHora() { return valorHora; }
    public void setValorHora(BigDecimal valorHora) { this.valorHora = valorHora; }

    public BigDecimal getDescontoMensalista() { return descontoMensalista; }
    public void setDescontoMensalista(BigDecimal descontoMensalista) { this.descontoMensalista = descontoMensalista; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
