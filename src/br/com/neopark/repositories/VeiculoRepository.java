package br.com.neopark.repositories;

import br.com.neopark.entities.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoRepository extends JpaRepository <Veiculo, Long> {
    
}