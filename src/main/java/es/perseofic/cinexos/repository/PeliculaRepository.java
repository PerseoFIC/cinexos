package es.perseofic.cinexos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.perseofic.cinexos.model.Pelicula;

import java.util.Optional;

public interface PeliculaRepository extends JpaRepository<Pelicula, Integer> {

	boolean existsByIdTmdb(Integer idTmdb);

	Optional<Pelicula> findByIdTmdb(Integer idTmdb);

}
