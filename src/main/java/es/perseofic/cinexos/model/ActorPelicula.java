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
@Table(name = "actor_pelicula")
public class ActorPelicula {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "id_actor")
	private Actor actor;

	@ManyToOne
	@JoinColumn(name = "id_pelicula")
	private Pelicula pelicula;

	private String personaje;

	@Column(name = "orden_creditos")
	private Integer ordenCreditos;

}
