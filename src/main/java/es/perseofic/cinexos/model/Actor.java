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
@Table(name = "actor")
public class Actor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "id_tmdb")
	private Integer idTmdb;

	@Column(name = "nombre_completo")
	private String nombreCompleto;

	@Column(name = "fecha_nacimiento")
	private Date fechaNacimiento;

	@Column(name = "lugar_nacimiento")
	private String lugarNacimiento;

	@Column(name = "ruta_foto")
	private String rutaFoto;

	@OneToMany(mappedBy = "actor", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ActorPelicula> peliculas = new ArrayList<>();

}
