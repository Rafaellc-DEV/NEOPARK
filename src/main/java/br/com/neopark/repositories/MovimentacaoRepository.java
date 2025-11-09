package br.com.neopark.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.neopark.entities.Movimentacao;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    List<Movimentacao> findByDataSaidaBetween(LocalDateTime inicio, LocalDateTime fim);
}