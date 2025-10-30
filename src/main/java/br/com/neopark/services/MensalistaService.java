package br.com.neopark.services;

import br.com.neopark.entities.Mensalista;
import br.com.neopark.entities.StatusPagamento;
import br.com.neopark.repositories.MensalistaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class MensalistaService {

    private final MensalistaRepository mensalistaRepo;

    public MensalistaService(MensalistaRepository mensalistaRepo) {
        this.mensalistaRepo = mensalistaRepo;
    }

    @Transactional
    public String registrarPagamento(String nomeCliente) {
        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente é obrigatório");
        }

        Mensalista mensalista = mensalistaRepo.findByNome(nomeCliente)
                .orElseThrow(() -> new IllegalArgumentException("Mensalista não encontrado: " + nomeCliente));

        LocalDate hoje = LocalDate.now();

        if (hoje.isBefore(mensalista.getDataVencimento())) {

            mensalista.setStatusPagamento(StatusPagamento.AGUARDANDO_VENCIMENTO);

            return StatusPagamento.AGUARDANDO_VENCIMENTO.getDescricao();
        }

        mensalista.setStatusPagamento(StatusPagamento.PAGO);

        LocalDate proximoVencimento = mensalista.getDataVencimento().plusMonths(1);
        mensalista.setDataVencimento(proximoVencimento);

        mensalistaRepo.save(mensalista);

        return StatusPagamento.PAGO.getDescricao();
    }
}