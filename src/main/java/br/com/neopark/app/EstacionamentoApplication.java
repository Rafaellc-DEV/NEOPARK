package br.com.neopark.app;

import br.com.neopark.entities.Tarifa;
import br.com.neopark.entities.Veiculo;
import br.com.neopark.services.EstacionamentoService;
import br.com.neopark.services.TarifaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

@SpringBootApplication(scanBasePackages = "br.com.neopark")
@EntityScan(basePackages = "br.com.neopark.entities")
@EnableJpaRepositories(basePackages = "br.com.neopark.repositories")
public class EstacionamentoApplication implements CommandLineRunner {

    @Autowired
    private EstacionamentoService service;

    @Autowired
    private TarifaService tarifaService;

    public static void main(String[] args) {
        SpringApplication.run(EstacionamentoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // Garante uma tarifa padrão na primeira execução (ex.: R$ 8,00/h e 0% de desconto)
        tarifaService.obterOuCriarPadrao();

        Scanner sc = new Scanner(System.in);
        boolean loop = true;
        while (loop) {
            System.out.println("""
┌──────────────────────────────────────────────┐
│               NEO PARK — MENU               │
├──────────────────────────────────────────────┤
│ 1) Registrar ENTRADA (avulso)               │
│ 2) Registrar ENTRADA (mensalista)           │
│ 3) Registrar SAÍDA (avulso)                 │
│ 4) Listar ESTACIONADOS                      │
│ 5) Buscar VEÍCULO por PLACA                 │
│ 6) Gerenciar TARIFAS                        │
│ 0) Sair                                     │
└──────────────────────────────────────────────┘""");
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

                        var veiculoOpt = service.buscarPorPlaca(placa);
                        if (veiculoOpt.isEmpty()) {
                            System.out.println("❌ Veículo não encontrado.");
                            break;
                        }
                        var v = veiculoOpt.get();

                        // Prévia do valor com base na tarifa vigente
                        LocalDateTime agora = LocalDateTime.now();
                        long minutos = Duration.between(v.getDataEntrada(), agora).toMinutes();
                        long horas = Math.max(1, (minutos + 59) / 60);

                        Tarifa tarifa = tarifaService.obterOuCriarPadrao();
                        BigDecimal valor = tarifa.getValorHora().multiply(BigDecimal.valueOf(horas));

                        if (Boolean.TRUE.equals(v.getMensalista())) {
                            BigDecimal descontoPct = tarifa.getDescontoMensalista(); // 0..100
                            if (descontoPct != null && descontoPct.signum() > 0) {
                                BigDecimal fator = BigDecimal.ONE.subtract(descontoPct.movePointLeft(2));
                                valor = valor.multiply(fator);
                            }
                        }
                        valor = valor.setScale(2, RoundingMode.HALF_UP);

                        System.out.println("=== Resumo da saída ===");
                        System.out.println("Placa: " + v.getPlaca());
                        System.out.println("Entrada: " + v.getDataEntrada());
                        System.out.println("Saída:   " + agora);
                        System.out.println("Mensalista: " + (Boolean.TRUE.equals(v.getMensalista()) ? "Sim" : "Não"));
                        System.out.println("Tarifa/hora vigente: R$ " + tarifa.getValorHora()
                                + " | Desconto mensalista: " + tarifa.getDescontoMensalista() + "%");
                        System.out.println("Valor devido (prévia): R$ " + valor);

                        System.out.print("Pressione ENTER para confirmar pagamento... ");
                        sc.nextLine();

                        // Registra de fato no serviço (remove e salva histórico)
                        valor = service.registrarSaidaAvulso(placa);
                        System.out.println("✅ Pagamento confirmado. Veículo removido.");
                        System.out.println("Valor cobrado: R$ " + valor);
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
                    case "5" -> {
                        System.out.print("Digite a placa do veículo: ");
                        String placaBusca = sc.nextLine().trim();
                        var veiculoOpt = service.buscarPorPlaca(placaBusca);
                        if (veiculoOpt.isPresent()) {
                            var v = veiculoOpt.get();
                            System.out.println("✅ Veículo encontrado:");
                            System.out.println("Placa: " + v.getPlaca());
                            System.out.println("Tipo: " + v.getTipo());
                            System.out.println("Modelo: " + v.getModelo());
                            System.out.println("Cor: " + v.getCor());
                            System.out.println("Entrada: " + v.getDataEntrada());
                            System.out.println("Mensalista: " + (Boolean.TRUE.equals(v.getMensalista()) ? "Sim" : "Não"));
                        } else {
                            System.out.println("❌ Veículo não encontrado no estacionamento.");
                        }
                    }
                    case "6" -> gerenciarTarifas(sc)
                    ;
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

    /** Opção de menu para HU11 – Gerenciar Tarifas do Estacionamento. */
    private void gerenciarTarifas(Scanner scanner) {
        try {
            Tarifa atual = tarifaService.obterOuCriarPadrao();
            System.out.println("\n=== GERENCIAR TARIFAS ===");
            System.out.println("Tarifa/hora atual: R$ " + atual.getValorHora());
            System.out.println("Desconto mensalista atual: " + atual.getDescontoMensalista() + "%");

            System.out.print("Novo valor/hora (R$): ");
            String inValor = scanner.nextLine().trim().replace(",", ".");
            BigDecimal novoValorHora = new BigDecimal(inValor);

            System.out.print("Novo desconto para mensalista (% 0–100): ");
            String inDesc = scanner.nextLine().trim().replace(",", ".");
            BigDecimal novoDesconto = new BigDecimal(inDesc);

            // Atualiza com validações (cenário desfavorável cobre zero/negativo)
            tarifaService.atualizar(novoValorHora, novoDesconto);

            System.out.println("✅ Tarifas atualizadas com sucesso e já aplicadas aos mensalistas.");
        } catch (IllegalArgumentException ex) {
            // Cenário Desfavorável: mensagem exigida pela história
            System.out.println("❌ " + ex.getMessage());
            System.out.println("Nada foi persistido.");
        } catch (Exception e) {
            System.out.println("❌ Erro inesperado ao salvar tarifas: " + e.getMessage());
        }
    }
}
