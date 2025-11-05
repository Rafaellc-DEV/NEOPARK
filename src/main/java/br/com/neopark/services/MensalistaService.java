package br.com.neopark.services;

import br.com.neopark.entities.Mensalista;
import br.com.neopark.entities.StatusPagamento;
import br.com.neopark.repositories.MensalistaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

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

    @Transactional
    public Mensalista cadastrarMensalista(String nome, String cpf, String telefone, String placaPrincipal) {
        // 1. Validação dos dados de entrada
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        if (cpf == null || cpf.isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório.");
        }

        if (placaPrincipal == null || placaPrincipal.isBlank()) {
            throw new IllegalArgumentException("Placa Principal é obrigatória.");
        }

        // 2. Regra de Negócio: Verificar se o CPF já existe
        if (mensalistaRepo.findByCpf(cpf).isPresent()) {
            throw new IllegalArgumentException("Já existe um mensalista com este CPF.");
        }

        // 3. Criar a nova entidade
        Mensalista novoMensalista = new Mensalista();
        novoMensalista.setNome(nome);
        novoMensalista.setCpf(cpf);
        novoMensalista.setTelefone(telefone); 
        novoMensalista.setPlacaPrincipal(placaPrincipal);
        // 4. Regra de Negócio: Definir o primeiro pagamento
        //    (Ex: o cliente paga no dia do cadastro e o próximo vencimento é daqui a 1 mês)
        novoMensalista.setStatusPagamento(StatusPagamento.PAGO);
        novoMensalista.setDataVencimento(LocalDate.now().plusMonths(1));
        // Nota: Ajuste os campos acima conforme a sua Entidade Mensalista

        // 5. Salvar no banco de dados e retornar o objeto salvo
        return mensalistaRepo.save(novoMensalista);
    }
    
    // Método auxiliar para o passo 3 (Refatoração da Saída)
    @Transactional(readOnly = true) // Apenas leitura, não modifica nada
    public Optional<Mensalista> buscarPorCpf(String cpf) {
        return mensalistaRepo.findByCpf(cpf);
    }
}