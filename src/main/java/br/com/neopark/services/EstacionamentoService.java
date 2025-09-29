package br.com.neopark.services;

import br.com.neopark.entities.RegistroSaida;
import br.com.neopark.entities.Veiculo;
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

    private final VeiculoRepository veiculoRepo;
    private final RegistroSaidaRepository saidaRepo;

    // Tarifa por hora (pode ser carregado de properties)
    private final BigDecimal TARIFA_HORA = new BigDecimal("10.00");

    public EstacionamentoService(VeiculoRepository veiculoRepo, RegistroSaidaRepository saidaRepo) {
        this.veiculoRepo = veiculoRepo;
        this.saidaRepo = saidaRepo;
    }

    @Transactional
    public Veiculo registrarEntrada(String placa, String tipo, String modelo, String cor, boolean mensalista) {
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("Placa é obrigatória");
        }
        if (veiculoRepo.existsByPlaca(placa.trim().toUpperCase())) {
            throw new IllegalArgumentException("Placa já cadastrada");
        }
        Veiculo v = new Veiculo(placa.trim().toUpperCase(), tipo, modelo, cor, mensalista);
        return veiculoRepo.save(v);
    }

    /**
     * Apenas consulta um veículo pela placa (sem remover/persistir saída).
     * Útil para exibir o resumo (entrada, saída prevista e valor) antes da confirmação.
     */
    @Transactional(readOnly = true)
    public Optional<Veiculo> buscarPorPlaca(String placa) {
        if (placa == null) return Optional.empty();
        return veiculoRepo.findByPlaca(placa.trim().toUpperCase());
    }

    @Transactional
    public BigDecimal registrarSaidaAvulso(String placa) {
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("Placa é obrigatória");
        }
        Veiculo v = veiculoRepo.findByPlaca(placa.trim().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado"));

        if (Boolean.TRUE.equals(v.getMensalista())) {
            // Mensalista: não há cobrança avulsa; apenas registra histórico e remove
            veiculoRepo.delete(v);
            saidaRepo.save(new RegistroSaida(v.getPlaca(), v.getDataEntrada(), LocalDateTime.now(), BigDecimal.ZERO));
            return BigDecimal.ZERO;
        }

        LocalDateTime agora = LocalDateTime.now();
        long minutos = Duration.between(v.getDataEntrada(), agora).toMinutes();
        long horasCobradas = Math.max(1, (minutos + 59) / 60); // hora iniciada arredonda para cima
        BigDecimal valor = TARIFA_HORA.multiply(BigDecimal.valueOf(horasCobradas))
                .setScale(2, RoundingMode.HALF_UP);

        // Persistir histórico e remover veículo dos estacionados
        saidaRepo.save(new RegistroSaida(v.getPlaca(), v.getDataEntrada(), agora, valor));
        veiculoRepo.delete(v);
        return valor;
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarEstacionados() {
        return veiculoRepo.findAll();
    }
}
