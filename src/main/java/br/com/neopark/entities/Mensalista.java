package br.com.neopark.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "mensalistas")
public class Mensalista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome; // Ex: "Ana Costa"

    @Column(nullable = false, length = 10, unique = true)
    private String placaPrincipal; // Usado para vincular ao veículo

    @Column(nullable = false)
    private LocalDate dataVencimento; // Data que a mensalidade vence

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPagamento statusPagamento;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorMensalidade; // Valor fixo da mensalidade

    // Construtores
    public Mensalista() {}

    public Mensalista(String nome, String placaPrincipal, LocalDate dataVencimento, BigDecimal valorMensalidade) {
        this.nome = nome;
        this.placaPrincipal = placaPrincipal;
        this.dataVencimento = dataVencimento;
        this.valorMensalidade = valorMensalidade;
        this.atualizarStatusBaseadoNaData();
    }

    public void atualizarStatusBaseadoNaData() {
        if (this.statusPagamento == StatusPagamento.PAGO) {
            // Se já pagou, não faz nada até o próximo vencimento
            return;
        }
        if (LocalDate.now().isBefore(dataVencimento)) {
            this.statusPagamento = StatusPagamento.AGUARDANDO_VENCIMENTO;
        } else {
            this.statusPagamento = StatusPagamento.PENDENTE;
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getPlacaPrincipal() { return placaPrincipal; }
    public void setPlacaPrincipal(String placaPrincipal) { this.placaPrincipal = placaPrincipal; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }
    public StatusPagamento getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(StatusPagamento statusPagamento) { this.statusPagamento = statusPagamento; }
    public BigDecimal getValorMensalidade() { return valorMensalidade; }
    public void setValorMensalidade(BigDecimal valorMensalidade) { this.valorMensalidade = valorMensalidade; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mensalista that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}