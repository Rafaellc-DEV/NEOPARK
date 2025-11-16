package br.com.neopark.services;

import br.com.neopark.entities.Mensalista;
import br.com.neopark.entities.StatusPagamento;
import br.com.neopark.repositories.MensalistaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MensalistaServiceTest {

    @Mock
    private MensalistaRepository mensalistaRepo;

    @InjectMocks
    private MensalistaService mensalistaService;

    private Mensalista mensalista;
    private final String NOME_VALIDO = "João Silva";
    private final String CPF_VALIDO = "123.456.789-00";
    private final String TELEFONE_VALIDO = "(81) 99999-9999";
    private final String PLACA_VALIDA = "ABC-1234";

    @BeforeEach
    void setup() {
        mensalista = new Mensalista();
        mensalista.setId(1L);
        mensalista.setNome(NOME_VALIDO);
        mensalista.setCpf(CPF_VALIDO);
        mensalista.setTelefone(TELEFONE_VALIDO);
        mensalista.setPlacaPrincipal(PLACA_VALIDA);
        mensalista.setDataVencimento(LocalDate.now().plusDays(5));
        mensalista.setStatusPagamento(StatusPagamento.PENDENTE);
    }

    //registrarPagamento(por nome)
    @Test
    void registrarPagamento_ComNomeValido_EAposVencimento_DeveMarcarComoPago() {
        mensalista.setDataVencimento(LocalDate.now().minusDays(1)); // vencido

        when(mensalistaRepo.findByNome(NOME_VALIDO)).thenReturn(Optional.of(mensalista));
        when(mensalistaRepo.save(any())).thenReturn(mensalista);

        Mensalista resultado = mensalistaService.registrarPagamento(NOME_VALIDO);

        assertEquals(StatusPagamento.PAGO, resultado.getStatusPagamento());
        assertEquals(LocalDate.now().minusDays(1).plusMonths(1), resultado.getDataVencimento());
        verify(mensalistaRepo).save(mensalista);
    }

    @Test
    void registrarPagamento_PagamentoAntesDoVencimento_DeveFicarAguardando() {
        mensalista.setStatusPagamento(StatusPagamento.PENDENTE);
        mensalista.setDataVencimento(LocalDate.now().plusDays(10));

        when(mensalistaRepo.findByNome(NOME_VALIDO)).thenReturn(Optional.of(mensalista));
        when(mensalistaRepo.save(any()))
            .thenAnswer(inv -> inv.getArgument(0));

        Mensalista res = mensalistaService.registrarPagamento(NOME_VALIDO);

        assertEquals(StatusPagamento.AGUARDANDO_VENCIMENTO, res.getStatusPagamento());
        verify(mensalistaRepo).save(mensalista);
    }

    @Test
    void registrarPagamento_NomeNulo_DeveLancar() {
        assertThrows(IllegalArgumentException.class,
                () -> mensalistaService.registrarPagamento(null));

        verifyNoInteractions(mensalistaRepo);
    }

    @Test
    void registrarPagamento_NomeVazio_DeveLancar() {
        assertThrows(IllegalArgumentException.class,
                () -> mensalistaService.registrarPagamento(""));

        verifyNoInteractions(mensalistaRepo);
    }

    @Test
    void registrarPagamento_NomeInexistente_DeveLancar() {
        when(mensalistaRepo.findByNome("Inexistente")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mensalistaService.registrarPagamento("Inexistente"));

        assertEquals("Mensalista não encontrado: Inexistente", ex.getMessage());
    }

    @Test
    void registrarPagamento_JaPago_DeveLancar() {
        mensalista.setStatusPagamento(StatusPagamento.PAGO);
        when(mensalistaRepo.findByNome(NOME_VALIDO)).thenReturn(Optional.of(mensalista));

        assertThrows(IllegalArgumentException.class,
                () -> mensalistaService.registrarPagamento(NOME_VALIDO));
    }

    //registrarPagamentoPorPlaca
    @Test
    void registrarPagamentoPorPlaca_Valida_DevePagarEAumentarVencimento() {
        mensalista.setDataVencimento(LocalDate.now().minusDays(1)); // vencido

        when(mensalistaRepo.findByPlacaPrincipal(PLACA_VALIDA)).thenReturn(Optional.of(mensalista));
        when(mensalistaRepo.save(any())).thenReturn(mensalista);

        Mensalista res = mensalistaService.registrarPagamentoPorPlaca(PLACA_VALIDA);

        assertEquals(StatusPagamento.PAGO, res.getStatusPagamento());
        assertEquals(LocalDate.now().minusDays(1).plusMonths(1), res.getDataVencimento());
    }

    @Test
void registrarPagamentoPorPlaca_ComPagamentoAdiantado_DevePagarSemMoverVencimento() {
    Mensalista m = new Mensalista();
    m.setNome("Caio");
    m.setPlacaPrincipal("ABC1234");
    m.setDataVencimento(LocalDate.now().plusDays(5));
    m.setStatusPagamento(StatusPagamento.AGUARDANDO_VENCIMENTO);

    when(mensalistaRepo.findByPlacaPrincipal("ABC1234"))
            .thenReturn(Optional.of(m));

    when(mensalistaRepo.save(any(Mensalista.class)))
            .thenAnswer(inv -> inv.getArgument(0)); // ← IMPORTANTE: retorna o objeto salvo

    Mensalista res = mensalistaService.registrarPagamentoPorPlaca("ABC1234");

    assertNotNull(res);
    assertEquals(StatusPagamento.PAGO, res.getStatusPagamento());
    assertEquals(m.getDataVencimento(), res.getDataVencimento()); // vencimento não muda
}


    @Test
    void registrarPagamentoPorPlaca_PlacaNula_DeveLancar() {
        assertThrows(IllegalArgumentException.class,
                () -> mensalistaService.registrarPagamentoPorPlaca(null));

        verifyNoInteractions(mensalistaRepo);
    }

    @Test
    void registrarPagamentoPorPlaca_PlacaVazia_DeveLancar() {
        assertThrows(IllegalArgumentException.class,
                () -> mensalistaService.registrarPagamentoPorPlaca(""));

        verifyNoInteractions(mensalistaRepo);
    }

    @Test
    void registrarPagamentoPorPlaca_Inexistente_DeveLancar() {
        when(mensalistaRepo.findByPlacaPrincipal("XYZ")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> mensalistaService.registrarPagamentoPorPlaca("XYZ"));

        assertEquals("Mensalista não encontrado com Placa: XYZ", ex.getMessage());
    }

    //cadastrarMensalista
    @Test
    void cadastrarMensalista_Valido_DeveSalvar() {
        when(mensalistaRepo.findByCpf(CPF_VALIDO)).thenReturn(Optional.empty());
        when(mensalistaRepo.findByPlacaPrincipal(PLACA_VALIDA)).thenReturn(Optional.empty());
        when(mensalistaRepo.save(any())).thenAnswer(inv -> {
            Mensalista m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });

        Mensalista res = mensalistaService.cadastrarMensalista(
                NOME_VALIDO, CPF_VALIDO, TELEFONE_VALIDO, PLACA_VALIDA
        );

        assertNotNull(res);
        assertEquals(StatusPagamento.PENDENTE, res.getStatusPagamento());
        assertEquals(LocalDate.now(), res.getDataVencimento());
    }

    @Test
    void cadastrarMensalista_CpfExistente_DeveLancar() {
        when(mensalistaRepo.findByCpf(CPF_VALIDO)).thenReturn(Optional.of(mensalista));

        assertThrows(IllegalArgumentException.class,
                () -> mensalistaService.cadastrarMensalista(NOME_VALIDO, CPF_VALIDO, TELEFONE_VALIDO, PLACA_VALIDA));
    }

    @Test
    void cadastrarMensalista_PlacaExistente_DeveLancar() {
        when(mensalistaRepo.findByCpf(CPF_VALIDO)).thenReturn(Optional.empty());
        when(mensalistaRepo.findByPlacaPrincipal(PLACA_VALIDA)).thenReturn(Optional.of(mensalista));

        assertThrows(IllegalArgumentException.class,
                () -> mensalistaService.cadastrarMensalista(NOME_VALIDO, CPF_VALIDO, TELEFONE_VALIDO, PLACA_VALIDA));
    }

    @Test
    void cadastrarMensalista_NomeNulo_DeveLancar() {
        assertThrows(IllegalArgumentException.class,
                () -> mensalistaService.cadastrarMensalista(null, CPF_VALIDO, TELEFONE_VALIDO, PLACA_VALIDA));
    }

    //buscarPorCpf
    @Test
    void buscarPorCpf_Existente_DeveRetornar() {
        when(mensalistaRepo.findByCpf(CPF_VALIDO)).thenReturn(Optional.of(mensalista));

        Optional<Mensalista> res = mensalistaService.buscarPorCpf(CPF_VALIDO);

        assertTrue(res.isPresent());
        assertEquals(mensalista, res.get());
    }

    @Test
    void buscarPorCpf_Inexistente_DeveRetornarVazio() {
        when(mensalistaRepo.findByCpf(CPF_VALIDO)).thenReturn(Optional.empty());

        Optional<Mensalista> res = mensalistaService.buscarPorCpf(CPF_VALIDO);

        assertFalse(res.isPresent());
    }


    //consultarPorPlaca
    @Test
    void consultarPorPlaca_Valida_DeveAtualizarStatus() {
        Mensalista m = spy(mensalista);

        when(mensalistaRepo.findByPlacaPrincipal(PLACA_VALIDA)).thenReturn(Optional.of(m));

        mensalistaService.consultarPorPlaca(PLACA_VALIDA);

        verify(m).atualizarStatusBaseadoNaData();
    }

    @Test
    void consultarPorPlaca_PlacaNula_DeveLancar() {
        assertThrows(IllegalArgumentException.class,
                () -> mensalistaService.consultarPorPlaca(null));

        verifyNoInteractions(mensalistaRepo);
    }

    //buscarPendentes
    @Test
    void buscarPendentes_DeveFiltrarSomentePendentes() {
        Mensalista pend1 = new Mensalista();
        pend1.setStatusPagamento(StatusPagamento.PENDENTE);

        Mensalista pend2 = new Mensalista();
        pend2.setStatusPagamento(StatusPagamento.PENDENTE);

        Mensalista pago = new Mensalista();
        pago.setStatusPagamento(StatusPagamento.PAGO);

        when(mensalistaRepo.findAll()).thenReturn(Arrays.asList(pend1, pago, pend2));

        List<Mensalista> res = mensalistaService.buscarPendentes();

        assertEquals(2, res.size());
        assertTrue(res.stream().allMatch(m -> m.getStatusPagamento() == StatusPagamento.PENDENTE));
    }

    @Test
    void buscarPendentes_SemPendentes_DeveRetornarVazio() {
        Mensalista pago = new Mensalista();
        pago.setStatusPagamento(StatusPagamento.PAGO);

        when(mensalistaRepo.findAll()).thenReturn(Arrays.asList(pago));

        List<Mensalista> res = mensalistaService.buscarPendentes();

        assertTrue(res.isEmpty());
    }
}
