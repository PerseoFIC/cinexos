package es.perseofic.cinexos.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Reto {

	private Actor actorA;
	private Actor actorB;
	private Actor actorC;
	private Pelicula peliculaAC;
	private Pelicula peliculaBC;

}
