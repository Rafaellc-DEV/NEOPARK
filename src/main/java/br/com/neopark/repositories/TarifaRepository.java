package br.com.neopark.repositories;

import br.com.neopark.entities.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
}
