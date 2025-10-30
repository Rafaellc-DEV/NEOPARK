package br.com.neopark.entities;

public enum StatusPagamento {
    PENDENTE("Pendência"),
    PAGO("Pagamento confirmado!"),
    AGUARDANDO_VENCIMENTO("Esperando pagamento");

    private final String descricao;

    StatusPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}