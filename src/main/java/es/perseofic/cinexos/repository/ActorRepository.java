package es.perseofic.cinexos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.perseofic.cinexos.model.Actor;

import java.util.List;
import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Integer> {

	Optional<Actor> findByIdTmdb(Integer idTmdb);

	@Query("SELECT a FROM Actor a WHERE (SELECT COUNT(ap) FROM ActorPelicula ap WHERE ap.actor = a) >= :minPeliculas")
	List<Actor> findActoresConMultiplesPeliculas(@Param("minPeliculas") Long minPeliculas);

	@Query("SELECT DISTINCT ap2.actor FROM ActorPelicula ap1 JOIN ActorPelicula ap2 ON ap1.pelicula = ap2.pelicula WHERE ap1.actor.id = :idActor AND ap2.actor.id <> :idActor")
	List<Actor> findColaboradores(@Param("idActor") Integer idActor);

	@Query("SELECT DISTINCT ap2.actor " +
			"FROM ActorPelicula ap1 " +
			"JOIN ActorPelicula ap2 ON ap1.pelicula = ap2.pelicula " +
			"WHERE ap1.actor.id = :idA " +
			"AND ap2.actor.id <> :idA " +
			"AND ap2.actor.id IN ( " +
			"SELECT ap4.actor.id " +
			"FROM ActorPelicula ap3 " +
			"JOIN ActorPelicula ap4 ON ap3.pelicula = ap4.pelicula " +
			"WHERE ap3.actor.id = :idB " +
			"AND ap4.actor.id <> :idB)")
	List<Actor> findActoresComunes(@Param("idA") Integer idA, @Param("idB") Integer idB);

}
