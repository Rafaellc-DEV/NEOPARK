package br.com.neopark.services;

import br.com.neopark.entities.Tarifa;
import br.com.neopark.repositories.TarifaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TarifaServiceTest {

    private TarifaRepository repo;
    private TarifaService service;

    @BeforeEach
    void setup() {
        repo = mock(TarifaRepository.class);
        service = new TarifaService(repo);
    }

    @Test
    void atualizar_deveSalvarQuandoParametrosValidos() {
        BigDecimal valorHora = new BigDecimal("10.50");
        BigDecimal desconto = new BigDecimal("15");

        when(repo.save(any(Tarifa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tarifa resultado = service.atualizar(valorHora, desconto);

        ArgumentCaptor<Tarifa> captor = ArgumentCaptor.forClass(Tarifa.class);
        verify(repo, atLeastOnce()).save(captor.capture());

        Tarifa salvo = captor.getValue();

        assertNotNull(salvo);
        assertEquals(0, salvo.getValorHora().compareTo(valorHora), "valorHora salvo deve ser igual ao informado");
        assertEquals(0, salvo.getDescontoMensalista().compareTo(desconto), "desconto salvo deve ser igual ao informado");

        assertNotNull(resultado);
        assertEquals(salvo, resultado);
    }

    @Test
    void atualizar_valorHoraNull_deveLancarIllegalArgumentException() {
        BigDecimal desconto = new BigDecimal("10");
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(null, desconto));
        verifyNoInteractions(repo);
    }

    @Test
    void atualizar_valorHoraNegativoOuZero_deveLancar() {
        BigDecimal desconto = new BigDecimal("5");
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(new BigDecimal("0"), desconto));
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(new BigDecimal("-1.00"), desconto));
        verifyNoInteractions(repo);
    }

    @Test
    void atualizar_descontoNull_deveLancar() {
        BigDecimal valorHora = new BigDecimal("5.00");
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(valorHora, null));
        verifyNoInteractions(repo);
    }

    @Test
    void atualizar_descontoForaDoIntervalo_deveLancar() {
        BigDecimal valorHora = new BigDecimal("5.00");
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(valorHora, new BigDecimal("-0.01")));
        assertThrows(IllegalArgumentException.class, () -> service.atualizar(valorHora, new BigDecimal("100.01")));
        verifyNoInteractions(repo);
    }
}
