package br.com.neopark.app;

import br.com.neopark.services.EstacionamentoService;
import br.com.neopark.entities.Veiculo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.math.BigDecimal;
import java.util.Scanner;

@SpringBootApplication(scanBasePackages = "br.com.neopark")
@EntityScan(basePackages = "br.com.neopark.entities")
@EnableJpaRepositories(basePackages = "br.com.neopark.repositories")
public class EstacionamentoApplication implements CommandLineRunner {

    @Autowired
    private EstacionamentoService service;

    public static void main(String[] args) {
        SpringApplication.run(EstacionamentoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Scanner sc = new Scanner(System.in);
        boolean loop = true;
        while (loop) {
            System.out.println("""
┌──────────────────────────────────────┐
│           NEO PARK — MENU            │
├──────────────────────────────────────┤
│ 1) Registrar ENTRADA (avulso)        │
│ 2) Registrar ENTRADA (mensalista)    │
│ 3) Registrar SAÍDA (avulso)          │
│ 4) Listar ESTACIONADOS               │
│ 0) Sair                              │
└──────────────────────────────────────┘""");
            System.out.print("> Selecione uma opção: ");
            String op = sc.nextLine().trim();

            try {
                switch (op) {
                    case "1" -> {
                        System.out.print("Placa: ");
                        String placa = sc.nextLine().trim();
                        System.out.print("Tipo (ex: Carro/Moto): ");
                        String tipo = sc.nextLine().trim();
                        System.out.print("Modelo: ");
                        String modelo = sc.nextLine().trim();
                        System.out.print("Cor: ");
                        String cor = sc.nextLine().trim();
                        Veiculo v = service.registrarEntrada(placa, tipo, modelo, cor, false);
                        System.out.println("✅ Entrada registrada. " + v);
                    }
                    case "2" -> {
                        System.out.print("Placa do mensalista: ");
                        String placa = sc.nextLine().trim();
                        System.out.print("Tipo (ex: Carro/Moto): ");
                        String tipo = sc.nextLine().trim();
                        System.out.print("Modelo: ");
                        String modelo = sc.nextLine().trim();
                        System.out.print("Cor: ");
                        String cor = sc.nextLine().trim();
                        Veiculo v = service.registrarEntrada(placa, tipo, modelo, cor, true);
                        System.out.println("✅ Entrada de mensalista registrada. " + v);
                    }
                    case "3" -> {
                        System.out.print("Placa para saída (avulso): ");
                        String placa = sc.nextLine().trim();

                        // Busca o veículo
                        var veiculoOpt = service.buscarPorPlaca(placa);
                        if (veiculoOpt.isEmpty()) {
                            System.out.println("❌ Veículo não encontrado.");
                            break;
                        }
                        var v = veiculoOpt.get();

                        // Calcula a prévia (sem remover ainda)
                        var agora = java.time.LocalDateTime.now();
                        long minutos = java.time.Duration.between(v.getDataEntrada(), agora).toMinutes();
                        long horas = Math.max(1, (minutos + 59) / 60);
                        java.math.BigDecimal valor = new java.math.BigDecimal("10.00")
                                .multiply(java.math.BigDecimal.valueOf(horas));

                        // Mostra detalhes
                        System.out.println("=== Resumo da saída ===");
                        System.out.println("Placa: " + v.getPlaca());
                        System.out.println("Entrada: " + v.getDataEntrada());
                        System.out.println("Saída:   " + agora);
                        System.out.println("Valor devido: R$ " + valor);

                        // Pede confirmação
                        System.out.print("Pressione ENTER para confirmar pagamento... ");
                        sc.nextLine();

                        // Agora registra de fato no serviço (remove e salva histórico)
                        valor = service.registrarSaidaAvulso(placa);
                        System.out.println("✅ Pagamento confirmado. Veículo removido.");
                    }

                    case "4" -> {
                        var lista = service.listarEstacionados();
                        if (lista.isEmpty()) {
                            System.out.println("Lista vazia: nenhum veículo estacionado.");
                        } else {
                            System.out.println("Estacionados (" + lista.size() + "):");
                            lista.forEach(System.out::println);
                        }
                    }
                    case "0" -> {
                        loop = false;
                        System.out.println("Encerrando...");
                    }
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("❌ " + e.getMessage());
            } catch (Exception e) {
                System.out.println("❌ Erro inesperado: " + e.getMessage());
            }
        }
    }
}