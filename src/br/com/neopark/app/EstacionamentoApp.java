package br.com.neopark.app;
import br.com.neopark.entities.Condutor;
import br.com.neopark.entities.Veiculo;
import java.util.ArrayList;
import java.util.Scanner;

public class EstacionamentoApp {

    public static void main(String[] args) {
        boolean sistemaRodando = true;
        Scanner scanner = new Scanner(System.in);
        ArrayList<Veiculo> veiculosEstacionados = new ArrayList<>();
        ArrayList<Condutor> condutoresCadastrados = new ArrayList<>();

        while(sistemaRodando){
            System.out.println("Sistema de Estacionamento NeoPark");
            System.out.println("Escolha uma das opções para realizar uma ação:");
            System.out.println("[1]- Adicionar veículo");
            System.out.println("[2]- Remover veículo");
            System.out.println("Em breve mais opções...");
            System.out.println("[0]- Sair");

            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 0:
                    sistemaRodando = false;
                    break;
                case 1:
                    System.out.println("\n Registrar Entrada de Veículo");

                    System.out.println("Digite a placa do veículo: ");
                    String placa = scanner.nextLine();

                    System.out.println("Digite o tipo do veículo: ");
                    String tipo = scanner.nextLine();

                    System.out.println("Digite o modelo do veículo: ");
                    String modelo = scanner.nextLine();

                    System.out.println("Digite a cor do veículo: ");
                    String cor = scanner.nextLine();

                    Veiculo novoVeiculo = new Veiculo (placa, tipo, modelo, cor);
                    veiculosEstacionados.add(novoVeiculo);

                    System.out.println("Entrada registrada com sucesso!");
                    System.out.println(novoVeiculo);

                    break;
                case 2:

            
                default:
                    System.out.println("Inválido");
                    break;
            }




            


        }
    }
    

}

