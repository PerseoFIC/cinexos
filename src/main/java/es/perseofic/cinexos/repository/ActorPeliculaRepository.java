package es.perseofic.cinexos.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import es.perseofic.cinexos.model.ActorPelicula;
import es.perseofic.cinexos.model.Pelicula;

public interface ActorPeliculaRepository extends JpaRepository<ActorPelicula, Integer> {

	@Query("SELECT ap1.pelicula FROM ActorPelicula ap1 " +
			"JOIN ActorPelicula ap2 ON ap1.pelicula = ap2.pelicula " +
			"WHERE ap1.actor.id = :idA AND ap2.actor.id = :idB " +
			"AND (:coleccionExcluida IS NULL OR ap1.pelicula.idColeccion IS NULL OR ap1.pelicula.idColeccion <> :coleccionExcluida)")
	List<Pelicula> findPeliculasEnComunExcluyendoColeccion(
			@Param("idA") Integer idA, 
			@Param("idB") Integer idB, 
			@Param("coleccionExcluida") Integer coleccionExcluida);

}
