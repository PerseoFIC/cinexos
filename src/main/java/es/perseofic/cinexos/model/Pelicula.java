package es.perseofic.cinexos.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "pelicula")
public class Pelicula {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "id_tmdb")
	private Integer idTmdb;

	private String titulo;

	@Column(name = "titulo_original")
	private String tituloOriginal;

	@Column(length = 4000)
	private String sinopsis;

	@Column(name = "fecha_estreno")
	private Date fechaEstreno;

	@Column(name = "idioma_original")
	private String idiomaOriginal;

	@Column(name = "nota_media")
	private Double notaMedia;

	@Column(name = "numero_votos")
	private Integer numeroVotos;

	@Column(name = "ruta_poster")
	private String rutaPoster;

	@Column(name = "id_coleccion")
	private Integer idColeccion;

	@Column(name = "nombre_coleccion")
	private String nombreColeccion;

	@OneToMany(mappedBy = "pelicula", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ActorPelicula> actores = new ArrayList<>();

}
