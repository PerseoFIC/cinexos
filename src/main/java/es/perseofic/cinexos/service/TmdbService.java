package es.perseofic.cinexos.service;

import es.perseofic.cinexos.model.Actor;
import es.perseofic.cinexos.model.ActorPelicula;
import es.perseofic.cinexos.model.Pelicula;
import es.perseofic.cinexos.repository.ActorPeliculaRepository;
import es.perseofic.cinexos.repository.ActorRepository;
import es.perseofic.cinexos.repository.PeliculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TmdbService {

	private final RestTemplate restTemplate = new RestTemplate();
	private final PeliculaRepository peliculaRepository;
	private final ActorRepository actorRepository;
	private final ActorPeliculaRepository actorPeliculaRepository;

	@Value("${tmdb.api.key}")
	private String apiKey;

	private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";

	// ======================================================
	// Carga inicial de películas y actores en la base de datos
	// ======================================================
	@Transactional
	public void cargarDatosIniciales() {

		// Paso 1: Obtener películas top rated y most voted
		List<Map<String, Object>> topRated = getMovies("/discover/movie?sort_by=vote_average.desc&vote_count.gte=1000&without_genres=16&with_runtime.gte=45&include_adult=false", 100);
		List<Map<String, Object>> mostVoted = getMovies("/discover/movie?sort_by=vote_count.desc&without_genres=16&with_runtime.gte=45&include_adult=false", 500);

		// Combinar sin duplicar
		Map<Integer, Map<String, Object>> allMovies = new LinkedHashMap<>();
		for (Map<String, Object> m : topRated) allMovies.put((Integer) m.get("id"), m);
		for (Map<String, Object> m : mostVoted) allMovies.putIfAbsent((Integer) m.get("id"), m);

		System.out.println("Total películas a procesar: " + allMovies.size());
		int indice = 1;

		// Paso 2: Procesar cada película
		for (Map<String, Object> movieMap : allMovies.values()) {

			Integer idTmdb = (Integer) movieMap.get("id");
			if (idTmdb == null) continue;

			// Evitar duplicados en BD
			if (peliculaRepository.existsByIdTmdb(idTmdb)) continue;

			System.out.println("Procesando película [" + indice++ + "]: " + movieMap.get("title"));

			Pelicula pelicula = new Pelicula();
			pelicula.setIdTmdb(idTmdb);
			pelicula.setTitulo((String) movieMap.get("title"));
			pelicula.setTituloOriginal((String) movieMap.get("original_title"));
			pelicula.setSinopsis(truncar((String) movieMap.get("overview"), 4000));
			pelicula.setFechaEstreno(parseDate((String) movieMap.get("release_date")));
			pelicula.setIdiomaOriginal((String) movieMap.get("original_language"));
			pelicula.setNotaMedia(convertToDouble(movieMap.get("vote_average")));
			pelicula.setNumeroVotos(convertToInt(movieMap.get("vote_count")));
			pelicula.setRutaPoster((String) movieMap.get("poster_path"));

			peliculaRepository.save(pelicula);

			// Paso 3: Obtener créditos (actores)
			String creditsUrl = TMDB_BASE_URL + "/movie/" + idTmdb + "/credits?api_key=" + apiKey;
			Map<String, Object> creditsResponse = restTemplate.getForObject(creditsUrl, Map.class);
			if (creditsResponse == null || !creditsResponse.containsKey("cast")) continue;

			List<Map<String, Object>> cast = (List<Map<String, Object>>) creditsResponse.get("cast");

			// Tomar los 20 primeros
			cast.stream().limit(20).forEach(c -> {
				Integer idActorTmdb = (Integer) c.get("id");
				if (idActorTmdb == null) {
					return;
				}

				// Buscar o crear el actor
				Actor actor = actorRepository.findByIdTmdb(idActorTmdb).orElseGet(() -> {
					Actor nuevo = getActorDetails(idActorTmdb);
					return nuevo != null ? actorRepository.save(nuevo) : null;
				});

				if (actor == null) {
					return;
				}

				// Crear relación actor_pelicula
				ActorPelicula relacion = new ActorPelicula();
				relacion.setActor(actor);
				relacion.setPelicula(pelicula);
				relacion.setPersonaje(truncar((String) c.get("character"), 255));
				relacion.setOrdenCreditos(convertToInt(c.get("order")));

				actorPeliculaRepository.save(relacion);
			});

			// Esperar un poco entre llamadas para no saturar la API
			try { Thread.sleep(150); } catch (InterruptedException ignored) {}
		}

		System.out.println("✅ Carga inicial completada correctamente");
	}

	// ======================================================
	// Carga inicial de colecciones en la base de datos
	// ======================================================
	@Transactional
	public void actualizarColecciones() {
		List<Pelicula> peliculas = peliculaRepository.findAll();

		for (Pelicula p : peliculas) {
			try {
				String url = "https://api.themoviedb.org/3/movie/" + p.getIdTmdb() + "?api_key=" + apiKey + "&language=es-ES";

				@SuppressWarnings("unchecked")
				Map<String, Object> resp = restTemplate.getForObject(url, Map.class);

				if (resp != null && resp.containsKey("belongs_to_collection")) {
					Object collectionObj = resp.get("belongs_to_collection");

					if (collectionObj instanceof Map) {
						Map<String, Object> collection = (Map<String, Object>) collectionObj;
						if (collection.containsKey("id")) {
							p.setIdColeccion(((Number) collection.get("id")).intValue());
						}
						if (collection.containsKey("name")) {
							p.setNombreColeccion((String) collection.get("name"));
						}
					}
				}

			} catch (Exception e) {
				System.out.println("⚠️ Error actualizando colección para '" + p.getTitulo() + "': " + e.getMessage());
			}
		}

		peliculaRepository.saveAll(peliculas);
		System.out.println("✅ Colecciones actualizadas correctamente (" + peliculas.size() + " películas).");
	}

	public List<Map<String, Object>> buscarActores(String query) {
		String url = String.format("%s/search/person?api_key=%s&language=es-ES&query=%s",
				TMDB_BASE_URL, apiKey, URLEncoder.encode(query, StandardCharsets.UTF_8));
		Map<String, Object> response = restTemplate.getForObject(url, Map.class);
		return (List<Map<String, Object>>) response.get("results");
	}

	public List<Map<String, Object>> buscarPeliculas(String query) {
		String url = String.format("%s/search/movie?api_key=%s&language=es-ES&query=%s",
				TMDB_BASE_URL, apiKey, URLEncoder.encode(query, StandardCharsets.UTF_8));
		Map<String, Object> response = restTemplate.getForObject(url, Map.class);
		return (List<Map<String, Object>>) response.get("results");
	}

	// ======================================================
	// Obtener detalles de un actor
	// ======================================================
	private Actor getActorDetails(Integer actorId) {
		try {
			String url = TMDB_BASE_URL + "/person/" + actorId + "?api_key=" + apiKey;
			Map<String, Object> data = restTemplate.getForObject(url, Map.class);
			if (data == null) return null;

			Actor actor = new Actor();
			actor.setIdTmdb(actorId);
			actor.setNombreCompleto((String) data.get("name"));
			actor.setFechaNacimiento(parseDate((String) data.get("birthday")));
			actor.setLugarNacimiento((String) data.get("place_of_birth"));
			actor.setRutaFoto((String) data.get("profile_path"));
			return actor;

		} catch (Exception e) {
			System.err.println("Error al obtener actor " + actorId + ": " + e.getMessage());
			return null;
		}
	}

	// ======================================================
	// Utilidades
	// ======================================================
	private List<Map<String, Object>> getMovies(String endpoint, int limit) {
		List<Map<String, Object>> result = new ArrayList<>();
		int page = 1;
		while (result.size() < limit) {
			// Determinar si el endpoint ya contiene "?" para usar ? o &
			String separator = endpoint.contains("?") ? "&" : "?";
			String url = TMDB_BASE_URL + endpoint + separator + "api_key=" + apiKey + "&language=es-ES&page=" + page;
			Map<String, Object> response = restTemplate.getForObject(url, Map.class);
			if (response == null || !response.containsKey("results")) break;
			List<Map<String, Object>> movies = (List<Map<String, Object>>) response.get("results");
			result.addAll(movies);
			if (movies.size() < 20) {
				break;
			}
			page++;
		}
		return result.stream().limit(limit).collect(Collectors.toList());
	}

	private String truncar(String texto, int max) {
		if (texto == null) return null;
		return texto.length() <= max ? texto : texto.substring(0, max);
	}

	private Date parseDate(String dateStr) {
		try {
			return dateStr == null || dateStr.isEmpty() ? null : java.sql.Date.valueOf(dateStr);
		} catch (Exception e) {
			return null;
		}
	}

	private Integer convertToInt(Object value) {
		return value instanceof Number ? ((Number) value).intValue() : null;
	}

	private Double convertToDouble(Object value) {
		return value instanceof Number ? ((Number) value).doubleValue() : null;
	}

}
