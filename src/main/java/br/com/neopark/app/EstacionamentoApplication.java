package br.com.neopark.app;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import br.com.neopark.entities.Mensalista;
import br.com.neopark.entities.Movimentacao;
import br.com.neopark.entities.Tarifa;
import br.com.neopark.entities.Veiculo;
import br.com.neopark.services.EstacionamentoService;
import br.com.neopark.services.MensalistaService;
import br.com.neopark.services.TarifaService;

@SpringBootApplication(scanBasePackages = "br.com.neopark")
@EntityScan(basePackages = "br.com.neopark.entities")
@EnableJpaRepositories(basePackages = "br.com.neopark.repositories")
public class EstacionamentoApplication implements CommandLineRunner {

    @Autowired
    private EstacionamentoService service;

    @Autowired
    private TarifaService tarifaService;

    @Autowired
    private MensalistaService mensalistaService;

    // Formato de data e hora padrão para entrada do usuário
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        SpringApplication.run(EstacionamentoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        tarifaService.obterOuCriarPadrao();

        Scanner sc = new Scanner(System.in);
        boolean loop = true;
        while (loop) {
            System.out.println("""
┌──────────────────────────────────────────────┐
│             NEO PARK — MENU                  │
├──────────────────────────────────────────────┤
│ 1) Registrar ENTRADA (Avulso/Mensalista)     │
│ 2) Registrar SAÍDA (avulso)                  │
│ 3) Listar ESTACIONADOS                       │
│ 4) Buscar VEÍCULO por PLACA                  │
│ 5) Gerenciar TARIFAS                         │
│ 6) Gerenciar Mensalistas                     │
│ 7) Ver HISTÓRICO                             │
│ 0) Sair                                      │
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

                        Optional<Mensalista> mensalistaOpt = mensalistaService.consultarPorPlaca(placa);

                        Veiculo v;
                        if (mensalistaOpt.isPresent()) {
                            System.out.println("...Reconhecido como Mensalista: " + mensalistaOpt.get().getNome());
                            v = service.registrarEntradaMensalista(placa, tipo, modelo, cor, mensalistaOpt.get());
                            System.out.println("✅ Entrada de Mensalista registrada. " + v);
                        } else {
                            v = service.registrarEntradaAvulso(placa, tipo, modelo, cor);
                            System.out.println("✅ Entrada Avulso registrada. " + v);
                        }
                    }

                    case "2" -> {
                        System.out.print("Placa para saída (avulso): ");
                        String placa = sc.nextLine().trim();

                        var veiculoOpt = service.buscarPorPlaca(placa);
                        if (veiculoOpt.isEmpty()) {
                            System.out.println("❌ Veículo não encontrado.");
                            break;
                        }
                        var v = veiculoOpt.get();

                        LocalDateTime agora = LocalDateTime.now();
                        long minutos = Duration.between(v.getDataEntrada(), agora).toMinutes();
                        long horas = Math.max(1, (minutos + 59) / 60);

                        Tarifa tarifa = tarifaService.obterOuCriarPadrao();
                        BigDecimal valor = tarifa.getValorHora().multiply(BigDecimal.valueOf(horas));

                        if (v.getMensalista() != null) {
                            // Reavaliar valor conforme regra no service (PAGO: ZERO; PENDENTE/VENCIDO: Cheio)
                            if (v.getMensalista().getStatusPagamento() == br.com.neopark.entities.StatusPagamento.PAGO) {
                                valor = BigDecimal.ZERO;
                            } // Caso contrário, valor fica o cheio calculado (avulso)
                        }
                        valor = valor.setScale(2, RoundingMode.HALF_UP);

                        System.out.println("=== Resumo da saída ===");
                        System.out.println("Placa: " + v.getPlaca());
                        System.out.println("Entrada: " + v.getDataEntrada().format(DATETIME_FORMATTER));
                        System.out.println("Saída:   " + agora.format(DATETIME_FORMATTER));
                        System.out.println("Permanência (minutos): " + minutos + " (Cobrado: " + horas + "h)");
                        System.out.println("Mensalista: " + (v.getMensalista() != null ? 
                            "Sim (" + v.getMensalista().getStatusPagamento().getDescricao() + ")" : "Não"));
                        System.out.println("Tarifa/hora vigente: R$ " + tarifa.getValorHora());
                        System.out.println("Valor devido (prévia): R$ " + valor);

                        System.out.print("Pressione ENTER para confirmar pagamento... ");
                        sc.nextLine();

                        valor = service.registrarSaidaAvulso(placa);
                        System.out.println("✅ Pagamento confirmado. Veículo removido.");
                        System.out.println("Valor cobrado: R$ " + valor);
                    }
                    case "3" -> {
                        var lista = service.listarEstacionados();
                        if (lista.isEmpty()) {
                            System.out.println("Lista vazia: nenhum veículo estacionado.");
                        } else {
                            System.out.println("Estacionados (" + lista.size() + "):");
                            lista.forEach(System.out::println);
                        }
                    }
                    case "4" -> {
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
                            System.out.println("Entrada: " + v.getDataEntrada().format(DATETIME_FORMATTER));
                            System.out.println("Mensalista: " + (v.getMensalista() != null ? 
                                "Sim (" + v.getMensalista().getStatusPagamento().getDescricao() + ")" : "Não"));
                        } else {
                            System.out.println("❌ Veículo não encontrado no estacionamento.");
                        }
                    }
                    case "5" -> gerenciarTarifas(sc);

                    case "6" -> gerenciarMensalistas(sc);
                    
                    case "7" -> gerenciarHistorico(sc);

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
                e.printStackTrace();
            }
        }
    }

    private void gerenciarHistorico(Scanner sc) {
        int subOpcao = -1;

        while (subOpcao != 0) {
            System.out.println("\n--- [Opção 7] Histórico de Movimentações ---");
            System.out.println("1. Listar todas as movimentações");
            System.out.println("2. Buscar por período de SAÍDA (dd/MM/yyyy HH:mm)");
            System.out.println("0. Voltar ao menu principal");
            System.out.print("> Escolha uma opção: ");

            String op = sc.nextLine().trim();
            try {
                subOpcao = Integer.parseInt(op);
            } catch (NumberFormatException e) {
                System.out.println("❌ Erro: Opção inválida. Digite um número.");
                subOpcao = -1;
                continue;
            }

            switch (subOpcao) {
                case 1:
                    listarHistoricoCompleto();
                    break;

                case 2:
                    buscarHistoricoPorPeriodo(sc);
                    break;
                
                case 0:
                    System.out.println("Voltando ao menu principal...");
                    break;

                default:
                    System.out.println("❌ Opção inválida. Tente novamente.");
                    break;
            }
        }
    }

    private void listarHistoricoCompleto() {
        try {
            List<Movimentacao> historico = service.listarHistorico();
            exibirHistorico(historico, "Histórico Completo");
        } catch (Exception e) {
            System.out.println("❌ ERRO ao listar histórico: " + e.getMessage());
        }
    }

    private void buscarHistoricoPorPeriodo(Scanner sc) {
        try {
            System.out.print("Data e hora INICIAL (dd/MM/yyyy HH:mm): ");
            String strInicio = sc.nextLine();
            LocalDateTime inicio = LocalDateTime.parse(strInicio, DATETIME_FORMATTER);

            System.out.print("Data e hora FINAL (dd/MM/yyyy HH:mm): ");
            String strFim = sc.nextLine();
            LocalDateTime fim = LocalDateTime.parse(strFim, DATETIME_FORMATTER);

            List<Movimentacao> historico = service.buscarHistoricoPorPeriodo(inicio, fim);
            exibirHistorico(historico, "Histórico de " + strInicio + " a " + strFim);
        } catch (java.time.format.DateTimeParseException e) {
            System.out.println("❌ ERRO: Formato de data/hora inválido. Use dd/MM/yyyy HH:mm (ex: 25/10/2025 10:30).");
        } catch (Exception e) {
            System.out.println("❌ ERRO ao buscar por período: " + e.getMessage());
        }
    }

    private void exibirHistorico(List<Movimentacao> historico, String titulo) {
        System.out.println("\n--- 📖 " + titulo + " (" + historico.size() + " Registros) ---");
        if (historico.isEmpty()) {
            System.out.println("  Nenhum registro encontrado.");
        } else {
            System.out.printf("%-10s | %-20s | %-20s | %-12s | %s%n", 
                "PLACA", "ENTRADA", "SAÍDA", "VALOR (R$)", "MENSALISTA");
            System.out.println("--------------------------------------------------------------------------");
            for (Movimentacao m : historico) {
                String entradaFmt = m.getDataEntrada().format(DATETIME_FORMATTER);
                String saidaFmt = m.getDataSaida().format(DATETIME_FORMATTER);
                String valorFmt = String.format("%.2f", m.getValorPago());
                String mensalistaFmt = m.isMensalista() ? "Sim" : "Não";

                System.out.printf("%-10s | %-20s | %-20s | %-12s | %s%n",
                    m.getPlaca(), entradaFmt, saidaFmt, valorFmt, mensalistaFmt);
            }
        }
    }

    private void gerenciarTarifas(Scanner scanner) {
        try {
            Tarifa atual = tarifaService.obterOuCriarPadrao();
            System.out.println("\n=== GERENCIAR TARIFAS (Opção 5) ===");
            System.out.println("Tarifa/hora atual: R$ " + atual.getValorHora());
            System.out.println("Desconto mensalista atual: " + atual.getDescontoMensalista() + "%");

            System.out.print("Novo valor/hora (R$): ");
            String inValor = scanner.nextLine().trim().replace(",", ".");
            BigDecimal novoValorHora = new BigDecimal(inValor);

            System.out.print("Novo desconto para mensalista (% 0–100): ");
            String inDesc = scanner.nextLine().trim().replace(",", ".");
            BigDecimal novoDesconto = new BigDecimal(inDesc);

            tarifaService.atualizar(novoValorHora, novoDesconto);

            System.out.println("✅ Tarifas atualizadas com sucesso e já aplicadas aos mensalistas.");
        } catch (IllegalArgumentException ex) {
            System.out.println("❌ " + ex.getMessage());
            System.out.println("Nada foi persistido.");
        } catch (Exception e) {
            System.out.println("❌ Erro inesperado ao salvar tarifas: " + e.getMessage());
        }
    }

    private void gerenciarMensalistas(Scanner sc) {
        int subOpcao = -1;

        while (subOpcao != 0) {
            System.out.println("\n--- [Opção 6] Gestão de Mensalistas ---");
            System.out.println("1. Cadastrar mensalista");
            System.out.println("2. Consultar situação (por Placa)");
            System.out.println("3. Registrar pagamento do mês");
            System.out.println("4. Listar mensalistas PENDENTES");
            System.out.println("0. Voltar ao menu principal");
            System.out.print("> Escolha uma opção: ");

            String op = sc.nextLine().trim();
            try {
                subOpcao = Integer.parseInt(op);
            } catch (NumberFormatException e) {
                System.out.println("❌ Erro: Opção inválida. Digite um número.");
                subOpcao = -1;
                continue;
            }

            switch (subOpcao) {
                case 1:
                    try {
                        System.out.print("Digite o Nome: ");
                        String nome = sc.nextLine();
                        System.out.print("Digite o CPF: ");
                        String cpf = sc.nextLine();
                        System.out.print("Digite o Telefone: ");
                        String telefone = sc.nextLine();
                        System.out.print("Digite a Placa Principal: ");
                        String placa = sc.nextLine();

                        Mensalista novo = mensalistaService.cadastrarMensalista(nome, cpf, telefone, placa);

                        System.out.println("\n✅ Mensalista cadastrado. Aguardando 1º pagamento (Opção 3).");
                        System.out.println("  Nome: " + novo.getNome());
                        System.out.println("  Placa: " + novo.getPlacaPrincipal());
                        System.out.println("  Status: " + novo.getStatusPagamento().getDescricao());
                        System.out.println("  Vencimento: " + novo.getDataVencimento());

                    } catch (IllegalArgumentException e) {
                        System.out.println("❌ ERRO ao cadastrar: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("❌ ERRO inesperado: " + e.getMessage());
                    }
                    break;

                case 2:
                    try {
                        System.out.print("Digite a Placa Principal para consulta: ");
                        String placa = sc.nextLine();

                        Optional<Mensalista> mensalistaOpt = mensalistaService.consultarPorPlaca(placa);

                        if (mensalistaOpt.isPresent()) {
                            Mensalista m = mensalistaOpt.get();
                            System.out.println("\n--- ℹ️ Situação do Mensalista ---");
                            System.out.println("  Nome: " + m.getNome());
                            System.out.println("  Placa: " + m.getPlacaPrincipal());
                            System.out.println("  CPF: " + m.getCpf());
                            System.out.println("  Data de Vencimento: " + m.getDataVencimento());
                            System.out.println("  Situação: " + m.getStatusPagamento().getDescricao());
                        } else {
                            System.out.println("❌ ERRO: Mensalista não encontrado com a placa: " + placa);
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("❌ ERRO ao consultar: " + e.getMessage());
                    }
                    break;

                case 3:
                    try {
                        System.out.print("Digite a PLACA do mensalista para registrar o pagamento: ");
                        String placaCliente = sc.nextLine();

                        Mensalista mensalistaAtualizado = mensalistaService.registrarPagamentoPorPlaca(placaCliente);

                        System.out.println("\n✅ Pagamento registrado com sucesso!");
                        System.out.println("  Cliente: " + mensalistaAtualizado.getNome());
                        System.out.println("  Placa: " + mensalistaAtualizado.getPlacaPrincipal());
                        System.out.println("  Situação: " + mensalistaAtualizado.getStatusPagamento().getDescricao());
                        System.out.println("  Novo vencimento: " + mensalistaAtualizado.getDataVencimento());

                    } catch (IllegalArgumentException e) {
                        System.out.println("❌ ERRO ao registrar pagamento: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("❌ ERRO: " + e.getMessage());
                    }
                    break;

                case 4:
                    try {
                        System.out.println("\n--- 💵 Mensalistas com Pagamento PENDENTE ---");
                        List<Mensalista> pendentes = mensalistaService.buscarPendentes();

                        if (pendentes.isEmpty()) {
                            System.out.println("  Nenhum mensalista com pendências encontrado.");
                        } else {
                            System.out.println("  Total de pendências: " + pendentes.size());
                            for (Mensalista m : pendentes) {
                                System.out.println("  --------------------");
                                System.out.println("  Nome: " + m.getNome());
                                System.out.println("  Placa: " + m.getPlacaPrincipal());
                                System.out.println("  Telefone: " + m.getTelefone());
                                System.out.println("  Vencido desde: " + m.getDataVencimento());
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("❌ ERRO ao buscar pendências: " + e.getMessage());
                    }
                    break;

                case 0:
                    break;

                default:
                    System.out.println("❌ Opção inválida. Tente novamente.");
                    break;
            }
        }
    }
}