package br.com.neopark.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration; 
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.neopark.entities.Mensalista;
import br.com.neopark.entities.Movimentacao;
import br.com.neopark.entities.StatusPagamento;
import br.com.neopark.entities.Tarifa;
import br.com.neopark.entities.Veiculo; 
import br.com.neopark.repositories.MovimentacaoRepository;
import br.com.neopark.repositories.VeiculoRepository;

@Service
public class EstacionamentoService {

    private final VeiculoRepository veiculoRepository;
    private final MovimentacaoRepository movimentacaoRepository; 
    private final TarifaService tarifaService;

    public EstacionamentoService(VeiculoRepository veiculoRepository,
                                 MovimentacaoRepository movimentacaoRepository, 
                                 TarifaService tarifaService) {
        this.veiculoRepository = veiculoRepository;
        this.movimentacaoRepository = movimentacaoRepository; 
        this.tarifaService = tarifaService;
    }

    // --- ENTRADA ---

    @Transactional
    public Veiculo registrarEntradaAvulso(String placa, String tipo, String modelo, String cor) {
        if (veiculoRepository.existsByPlaca(placa.toUpperCase())) {
            throw new IllegalArgumentException("Veículo com placa " + placa + " já está estacionado.");
        }
        Veiculo veiculo = new Veiculo(placa, tipo, modelo, cor, null);
        return veiculoRepository.save(veiculo);
    }

    @Transactional
    public Veiculo registrarEntradaMensalista(String placa, String tipo, String modelo, String cor, Mensalista mensalista) {
        if (veiculoRepository.existsByPlaca(placa.toUpperCase())) {
            throw new IllegalArgumentException("Veículo com placa " + placa + " já está estacionado.");
        }
        if (mensalista == null) {
             throw new IllegalArgumentException("Mensalista não pode ser nulo para esta operação.");
           }
        Veiculo veiculo = new Veiculo(placa, tipo, modelo, cor, mensalista);
        return veiculoRepository.save(veiculo);
    }

    // --- SAÍDA E CÁLCULO DE COBRANÇA ---

    @Transactional
    public BigDecimal registrarSaidaAvulso(String placa) {
        Veiculo veiculo = veiculoRepository.findByPlaca(placa.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Veículo com placa " + placa + " não encontrado na lista de estacionados."));

        LocalDateTime entrada = veiculo.getDataEntrada();
        LocalDateTime saida = LocalDateTime.now();

        // 1. Cálculo do tempo e horas cheias (mínimo de 1 hora)
        long minutos = Duration.between(entrada, saida).toMinutes();
        long horasCobradas = Math.max(1, (minutos + 59) / 60);

        Tarifa tarifa = tarifaService.obterOuCriarPadrao();
        BigDecimal valorHora = tarifa.getValorHora();
        BigDecimal valorFinal = BigDecimal.ZERO;


        if (veiculo.getMensalista() != null) {
            // Cliente é mensalista: aplica a nova regra de status
            StatusPagamento status = veiculo.getMensalista().getStatusPagamento();
            
            if (status == StatusPagamento.PAGO) {
                // Gratuito
                valorFinal = BigDecimal.ZERO;
            } else {
                // PENDENTE ou VENCIDO: Cobra como avulso (valor cheio)
                valorFinal = valorHora.multiply(BigDecimal.valueOf(horasCobradas));
            }
        } else {
            // Cliente avulso (sem mensalista): Cobra valor cheio
            valorFinal = valorHora.multiply(BigDecimal.valueOf(horasCobradas));
        }
        
        // Garante 2 casas decimais
        valorFinal = valorFinal.setScale(2, RoundingMode.HALF_UP);

        // 2. Cria registro de Movimentação (Histórico)
        Movimentacao movimentacao = new Movimentacao(
                veiculo.getPlaca(),
                entrada,
                saida,
                valorFinal,
                veiculo.getMensalista() != null
        );
        movimentacaoRepository.save(movimentacao);

        // 3. Remove Veiculo da lista de estacionados
        veiculoRepository.delete(veiculo);

        return valorFinal;
    }

    // --- CONSULTAS DE VEÍCULOS ESTACIONADOS ---

    @Transactional(readOnly = true)
    public Optional<Veiculo> buscarPorPlaca(String placa) {
        return veiculoRepository.findByPlaca(placa.toUpperCase());
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarEstacionados() {
        return veiculoRepository.findAll();
    }

    // --- CONSULTAS DE HISTÓRICO (MOVIMENTAÇÃO) ---

    /**
     * Retorna todos os registros de movimentação no histórico.
     * @return Lista de Movimentacao.
     */
    @Transactional(readOnly = true)
    public List<Movimentacao> listarHistorico() {
        return movimentacaoRepository.findAll();
    }

    /**
     * Busca todas as movimentações que tiveram a saída registrada dentro de um período específico.
     * @param inicio O início do período (Data e Hora).
     * @param fim O fim do período (Data e Hora).
     * @return Lista de Movimentacao.
     */
    @Transactional(readOnly = true)
    public List<Movimentacao> buscarHistoricoPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        // Usa o método customizado no MovimentacaoRepository
        return movimentacaoRepository.findByDataSaidaBetween(inicio, fim);
    }
}