package br.com.neopark.services;

import br.com.neopark.entities.Mensalista;
import br.com.neopark.entities.StatusPagamento;
import br.com.neopark.repositories.MensalistaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MensalistaService {

    private final MensalistaRepository mensalistaRepo;

    public MensalistaService(MensalistaRepository mensalistaRepo) {
        this.mensalistaRepo = mensalistaRepo;
    }

    @Transactional
    public Mensalista registrarPagamento(String nomeCliente) {
        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente é obrigatório");
        }

        Mensalista mensalista = mensalistaRepo.findByNome(nomeCliente)
                .orElseThrow(() -> new IllegalArgumentException("Mensalista não encontrado: " + nomeCliente));

        if (mensalista.getDataVencimento() == null) {
            throw new IllegalStateException("Data de vencimento não pode ser nula para registrar pagamento.");
        }

        if (mensalista.getStatusPagamento() == StatusPagamento.PAGO) {
            throw new IllegalArgumentException("Pagamento já está confirmado para este vencimento.");
        }

        LocalDate hoje = LocalDate.now();
        LocalDate vencimento = mensalista.getDataVencimento();

        //Pagamento adiantado
        if (hoje.isBefore(vencimento)) {
            mensalista.setStatusPagamento(StatusPagamento.AGUARDANDO_VENCIMENTO);
        } else {
            mensalista.setStatusPagamento(StatusPagamento.PAGO);
            mensalista.setDataVencimento(vencimento.plusMonths(1));
        }

        return mensalistaRepo.save(mensalista);
    }

    @Transactional
    public Mensalista registrarPagamentoPorPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("Placa é obrigatória");
        }

        Mensalista mensalista = mensalistaRepo.findByPlacaPrincipal(placa)
                .orElseThrow(() -> new IllegalArgumentException("Mensalista não encontrado com Placa: " + placa));

        if (mensalista.getDataVencimento() == null) {
            throw new IllegalStateException("Data de vencimento não pode ser nula para registrar pagamento.");
        }

        if (mensalista.getStatusPagamento() == StatusPagamento.PAGO) {
            throw new IllegalArgumentException("Pagamento já está confirmado para este vencimento.");
        }

        LocalDate hoje = LocalDate.now();
        LocalDate vencimento = mensalista.getDataVencimento();
        StatusPagamento status = mensalista.getStatusPagamento();

        // Pagamento adiantado (antes do vencimento)
        if (hoje.isBefore(vencimento) && status == StatusPagamento.AGUARDANDO_VENCIMENTO) {
            mensalista.setStatusPagamento(StatusPagamento.PAGO);
            // Vencimento permanece o mesmo
        }
        else {
            mensalista.setStatusPagamento(StatusPagamento.PAGO);
            mensalista.setDataVencimento(vencimento.plusMonths(1));
        }

        return mensalistaRepo.save(mensalista);
    }

    @Transactional
    public Mensalista cadastrarMensalista(String nome, String cpf, String telefone, String placaPrincipal) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório.");
        }
        if (placaPrincipal == null || placaPrincipal.isBlank()) {
            throw new IllegalArgumentException("Placa Principal é obrigatória.");
        }

        if (mensalistaRepo.findByCpf(cpf).isPresent()) {
            throw new IllegalArgumentException("Já existe um mensalista com este CPF.");
        }
        if (mensalistaRepo.findByPlacaPrincipal(placaPrincipal).isPresent()) {
            throw new IllegalArgumentException("Já existe um mensalista com esta Placa.");
        }

        Mensalista novoMensalista = new Mensalista();
        novoMensalista.setNome(nome);
        novoMensalista.setCpf(cpf);
        novoMensalista.setTelefone(telefone);
        novoMensalista.setPlacaPrincipal(placaPrincipal);

        novoMensalista.setStatusPagamento(StatusPagamento.PENDENTE);
        novoMensalista.setDataVencimento(LocalDate.now());

        return mensalistaRepo.save(novoMensalista);
    }

    @Transactional(readOnly = true)
    public Optional<Mensalista> buscarPorCpf(String cpf) {
        return mensalistaRepo.findByCpf(cpf);
    }

    @Transactional(readOnly = true)
    public Optional<Mensalista> consultarPorPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("A placa é obrigatória para a consulta.");
        }

        Optional<Mensalista> mensalistaOpt = mensalistaRepo.findByPlacaPrincipal(placa);

        if (mensalistaOpt.isPresent()) {
            mensalistaOpt.get().atualizarStatusBaseadoNaData();
        }

        return mensalistaOpt;
    }

    @Transactional(readOnly = true)
    public List<Mensalista> buscarPendentes() {
        List<Mensalista> todos = mensalistaRepo.findAll();

        // Atualiza status de todos (baseado na data atual)
        todos.forEach(m -> {
            // Evita NullPointerException em dados sujos
            if (m.getDataVencimento() != null) {
                m.atualizarStatusBaseadoNaData();
            }
        });

        return todos.stream()
                .filter(m -> m.getStatusPagamento() == StatusPagamento.PENDENTE)
                .collect(Collectors.toList());
    }
}
