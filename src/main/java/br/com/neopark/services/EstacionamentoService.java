package br.com.neopark.services;

import br.com.neopark.entities.Mensalista;
import br.com.neopark.entities.RegistroSaida;
import br.com.neopark.entities.StatusPagamento;
import br.com.neopark.entities.Veiculo;
import br.com.neopark.entities.Tarifa;
import br.com.neopark.repositories.RegistroSaidaRepository;
import br.com.neopark.repositories.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EstacionamentoService {

        private final VeiculoRepository veiculoRepository;
        private final RegistroSaidaRepository saidaRepository;
        private final TarifaService tarifaService;

        public EstacionamentoService(VeiculoRepository veiculoRepository,
                                    RegistroSaidaRepository saidaRepository,
                                    TarifaService tarifaService) {
            this.veiculoRepository = veiculoRepository;
            this.saidaRepository = saidaRepository;
            this.tarifaService = tarifaService;
        }

        @Transactional
        public Veiculo registrarEntradaAvulso(String placa, String tipo, String modelo, String cor) {
            // Validação (ex: verificar se a placa já está estacionada)
            if (veiculoRepository.existsByPlaca(placa)) {
                throw new IllegalArgumentException("Veículo com esta placa já está no estacionamento.");
            }

            Veiculo veiculo = new Veiculo(placa, tipo, modelo, cor, null);

            return veiculoRepository.save(veiculo);
        }

        @Transactional
        public Veiculo registrarEntradaMensalista(String placa, String tipo, String modelo, String cor, Mensalista mensalista) {
            // Validação
            if (veiculoRepository.existsByPlaca(placa)) {
                throw new IllegalArgumentException("Veículo com esta placa já está no estacionamento.");
            }
            if (mensalista == null) {
                throw new IllegalArgumentException("Mensalista não pode ser nulo para esta operação.");
            }

            Veiculo veiculo = new Veiculo(placa, tipo, modelo, cor, mensalista);

            return veiculoRepository.save(veiculo);
        }

        /**
         * Apenas consulta um veículo pela placa (sem remover/persistir saída).
         * Útil para exibir o resumo (entrada, saída prevista e valor) antes da confirmação.
         */
        @Transactional(readOnly = true)
        public Optional<Veiculo> buscarPorPlaca(String placa) {
            if (placa == null) return Optional.empty();
            return veiculoRepository.findByPlaca(placa.trim().toUpperCase());
        }

        @Transactional
        public BigDecimal registrarSaidaAvulso(String placa) {
            if (placa == null || placa.isBlank()) {
                throw new IllegalArgumentException("Placa é obrigatória");
            }

            Veiculo v = veiculoRepository.findByPlaca(placa.trim().toUpperCase())
                    .orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado"));

            LocalDateTime agora = LocalDateTime.now();
            long minutos = Duration.between(v.getDataEntrada(), agora).toMinutes();
            long horasCobradas = Math.max(1, (minutos + 59) / 60);

            Tarifa tarifa = tarifaService.obterOuCriarPadrao();
            BigDecimal valorHora = tarifa.getValorHora();
            BigDecimal valor = BigDecimal.ZERO; // Valor começa em zero

            // --- ESTA É A NOVA LÓGICA DE NEGÓCIO ---
            
            // 1. Verificamos se o veículo PERTENCE a um mensalista
            if (v.getMensalista() != null) {
                // 2. Se sim, verificamos o STATUS do pagamento
                StatusPagamento status = v.getMensalista().getStatusPagamento();
                
                if (status == StatusPagamento.PAGO) {
                    // 3. Se está PAGO, a saída é gratuita!
                    valor = BigDecimal.ZERO;
                    System.out.println("Mensalista com pagamento em dia. Saída gratuita.");
                } else {
                    // 4. Se está VENCIDO ou PENDENTE, cobra como avulso SEM desconto
                    System.out.println("Mensalista com pagamento pendente. Cobrando valor de avulso.");
                    valor = valorHora.multiply(BigDecimal.valueOf(horasCobradas));
                }
                
            } else {
                // 5. Se for um cliente AVULSO (mensalista == null), cobra normal
                System.out.println("Cliente avulso. Cobrando valor normal.");
                valor = valorHora.multiply(BigDecimal.valueOf(horasCobradas));
            }

            valor = valor.setScale(2, RoundingMode.HALF_UP);

            // Persistir histórico e remover veículo dos estacionados
            saidaRepository.save(new RegistroSaida(v.getPlaca(), v.getDataEntrada(), agora, valor));
            veiculoRepository.delete(v);

            return valor;
        }

        @Transactional(readOnly = true)
        public List<Veiculo> listarEstacionados() {
            return veiculoRepository.findAll();
        }
}

