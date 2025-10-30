package br.com.neopark.repositories;

import br.com.neopark.entities.Mensalista;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MensalistaRepository extends JpaRepository<Mensalista, Long> {

    /**
     * Busca um mensalista pelo nome, conforme solicitado na HU4 ("Ana Costa").
     */
    Optional<Mensalista> findByNome(String nome);

    /**
     * Busca um mensalista pela placa principal (útil na integração com EstacionamentoService).
     */
    Optional<Mensalista> findByPlacaPrincipal(String placa);
}