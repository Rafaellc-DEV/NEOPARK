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

    /**
     * Registra o pagamento (por NOME).
     * @deprecated Use registrarPagamentoPorPlaca para evitar erros de duplicidade.
     */
    @Transactional
    public Mensalista registrarPagamento(String nomeCliente) {
        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente é obrigatório");
        }

        Mensalista mensalista = mensalistaRepo.findByNome(nomeCliente)
                .orElseThrow(() -> new IllegalArgumentException("Mensalista não encontrado: " + nomeCliente));

        if (mensalista.getStatusPagamento() == StatusPagamento.PAGO) {
            throw new IllegalArgumentException("Pagamento já está confirmado para este vencimento.");
        }

        LocalDate hoje = LocalDate.now();

        if (hoje.isBefore(mensalista.getDataVencimento())) {
            mensalista.setStatusPagamento(StatusPagamento.AGUARDANDO_VENCIMENTO);
        } else {
            mensalista.setStatusPagamento(StatusPagamento.PAGO);
            LocalDate proximoVencimento = mensalista.getDataVencimento().plusMonths(1);
            mensalista.setDataVencimento(proximoVencimento);
        }

        return mensalistaRepo.save(mensalista);
    }

    /**
     * Registra o pagamento (por PLACA) e retorna a entidade Mensalista atualizada.
     */
    @Transactional
    public Mensalista registrarPagamentoPorPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("Placa é obrigatória");
        }

        Mensalista mensalista = mensalistaRepo.findByPlacaPrincipal(placa)
                .orElseThrow(() -> new IllegalArgumentException("Mensalista não encontrado com Placa: " + placa));

        // Se o status já é PAGO (para o vencimento atual), não permite pagar de novo.
        if (mensalista.getStatusPagamento() == StatusPagamento.PAGO) {
            throw new IllegalArgumentException("Pagamento já está confirmado para este vencimento.");
        }

        LocalDate hoje = LocalDate.now();

        // Se o status era AGUARDANDO_VENCIMENTO (pagou adiantado)
        if (hoje.isBefore(mensalista.getDataVencimento()) && mensalista.getStatusPagamento() == StatusPagamento.AGUARDANDO_VENCIMENTO) {
            mensalista.setStatusPagamento(StatusPagamento.PAGO);
            // Não mexe no vencimento, pois já estava correto.
        }
        // Se o status era PENDENTE (venceu hoje ou estava atrasado)
        // OU se é o primeiro pagamento (vencimento=hoje, status=PENDENTE)
        else {
            mensalista.setStatusPagamento(StatusPagamento.PAGO);
            LocalDate proximoVencimento = mensalista.getDataVencimento().plusMonths(1);
            mensalista.setDataVencimento(proximoVencimento);
        }

        return mensalistaRepo.save(mensalista);
    }


    @Transactional
    public Mensalista cadastrarMensalista(String nome, String cpf, String telefone, String placaPrincipal) {
        // 1. Validação
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório.");
        }
        if (placaPrincipal == null || placaPrincipal.isBlank()) {
            throw new IllegalArgumentException("Placa Principal é obrigatória.");
        }

        // 2. Regras de Negócio (Campos únicos)
        if (mensalistaRepo.findByCpf(cpf).isPresent()) {
            throw new IllegalArgumentException("Já existe um mensalista com este CPF.");
        }
        if (mensalistaRepo.findByPlacaPrincipal(placaPrincipal).isPresent()) {
            throw new IllegalArgumentException("Já existe um mensalista com esta Placa.");
        }

        // 3. Criar a nova entidade
        Mensalista novoMensalista = new Mensalista();
        novoMensalista.setNome(nome);
        novoMensalista.setCpf(cpf);
        novoMensalista.setTelefone(telefone);
        novoMensalista.setPlacaPrincipal(placaPrincipal);

        // 4. Regra de Negócio: Cliente entra como PENDENTE
        novoMensalista.setStatusPagamento(StatusPagamento.PENDENTE);
        novoMensalista.setDataVencimento(LocalDate.now()); // Vencimento é hoje

        // 5. Salvar e retornar
        return mensalistaRepo.save(novoMensalista);
    }

    @Transactional(readOnly = true)
    public Optional<Mensalista> buscarPorCpf(String cpf) {
        return mensalistaRepo.findByCpf(cpf);
    }

    /**
     * Método para a Opção 2 do menu: Consultar situação pela Placa.
     */
    @Transactional(readOnly = true)
    public Optional<Mensalista> consultarPorPlaca(String placa) {
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("A placa é obrigatória para a consulta.");
        }

        Optional<Mensalista> mensalistaOpt = mensalistaRepo.findByPlacaPrincipal(placa);

        if (mensalistaOpt.isPresent()) {
            // Atualiza o status (Ex: se hoje > vencimento, vira PENDENTE)
            mensalistaOpt.get().atualizarStatusBaseadoNaData();
        }

        return mensalistaOpt;
    }

    // *** NOVO MÉTODO ADICIONADO (Opção 4 do menu) ***
    /**
     * Busca todos os mensalistas e retorna apenas os que estão PENDENTES.
     * Este método atualiza o status de todos os mensalistas na memória
     * (baseado na data atual) antes de filtrar.
     * * @return Lista de mensalistas com status PENDENTE.
     */
    @Transactional(readOnly = true) // Apenas lê, não salva as atualizações de status
    public List<Mensalista> buscarPendentes() {
        List<Mensalista> todos = mensalistaRepo.findAll();

        // 1. Atualiza o status de todos NA MEMÓRIA para a data de hoje
        // (Isso garante que quem venceu hoje apareça como PENDENTE)
        todos.forEach(Mensalista::atualizarStatusBaseadoNaData);

        // 2. Filtra e retorna apenas os PENDENTES
        return todos.stream()
                .filter(m -> m.getStatusPagamento() == StatusPagamento.PENDENTE)
                .collect(Collectors.toList());
    }
}