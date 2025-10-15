package br.com.neopark.services;

import br.com.neopark.entities.Tarifa;
import br.com.neopark.repositories.TarifaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TarifaService {

    private final TarifaRepository repo;

    public TarifaService(TarifaRepository repo) {
        this.repo = repo;
    }

    /** Busca a tarifa vigente; cria uma padrão se ainda não existir. */
    @Transactional
    public Tarifa obterOuCriarPadrao() {
        return repo.findAll().stream().findFirst()
                .orElseGet(() -> repo.save(new Tarifa(new BigDecimal("8.00"), new BigDecimal("0.00"))));
    }

    /** Atualiza valores com validação da HU11. */
    @Transactional
    public Tarifa atualizar(BigDecimal valorHora, BigDecimal descontoMensalista) {
        if (valorHora == null || valorHora.signum() <= 0) {
            throw new IllegalArgumentException("Valor da tarifa não pode ser zero ou negativo");
        }
        if (descontoMensalista == null || descontoMensalista.compareTo(new BigDecimal("0")) < 0
                || descontoMensalista.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentual de desconto precisa estar entre 0 e 100");
        }
        Tarifa t = obterOuCriarPadrao();
        t.setValorHora(valorHora);
        t.setDescontoMensalista(descontoMensalista);
        return repo.save(t);
    }
}
