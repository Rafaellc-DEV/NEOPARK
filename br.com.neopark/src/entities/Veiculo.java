package entities;

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

    @Override
    public String toString() {
        return "Placa: " + placa + "\nTipo: " + tipo + "\nModelo: " + modelo + "\nCor: " + cor;
    }

}
