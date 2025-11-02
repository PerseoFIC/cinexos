package es.perseofic.cinexos.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "partida")
public class Partida {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_actor_a")
	private Actor actorA;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_actor_b")
	private Actor actorB;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_actor_c")
	private Actor actorC;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_pelicula_ac")
	private Pelicula peliculaAC;

	@ManyToOne(optional = false)
	@JoinColumn(name = "id_pelicula_bc")
	private Pelicula peliculaBC;

	@Column(name = "intento_nombre_actor_c", nullable = false)
	private String intentoNombreActorC;

	@Column(name = "intento_titulo_pelicula_ac", nullable = false)
	private String intentoTituloPeliculaAC;

	@Column(name = "intento_titulo_pelicula_bc", nullable = false)
	private String intentoTituloPeliculaBC;

	@Column(name = "exito", nullable = false)
	private Boolean exito;

	@Column(name = "pistas_usadas", nullable = false)
	private Integer pistasUsadas;

}
