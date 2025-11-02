package es.perseofic.cinexos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.perseofic.cinexos.model.Partida;

public interface PartidaRepository extends JpaRepository<Partida, Integer> {
}
