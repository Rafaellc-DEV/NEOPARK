package br.com.neopark.entities;

public class Veiculo {

    private String placa;
    private String tipo;
    private String modelo;
    private String cor;

    public Veiculo(String placa, String tipo, String modelo, String cor) {

        this.placa = placa;
        this.tipo = tipo;
        this.modelo = modelo;
        this.cor = cor;
        
    }

    public String getPlaca() { return placa; }
    public String getTipo() { return tipo; }
    public String getModelo() { return modelo; }
    public String getCor() { return cor; }

    public void setPlaca(String placa) { this.placa = placa; }
    public void setTipo(String  tipo) { this.tipo = tipo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public void setCor(String cor) { this.cor = cor; }

    @Override
    public String toString() {
        return "\nPlaca: " + placa + "\nTipo: " + tipo + "\nModelo: " + modelo + "\nCor: " + cor;
    }

}
