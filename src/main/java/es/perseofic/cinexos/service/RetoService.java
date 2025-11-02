package es.perseofic.cinexos.service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import es.perseofic.cinexos.model.Actor;
import es.perseofic.cinexos.model.Pelicula;
import es.perseofic.cinexos.model.Reto;
import es.perseofic.cinexos.repository.ActorPeliculaRepository;
import es.perseofic.cinexos.repository.ActorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetoService {

	private final ActorRepository actorRepository;
	private final ActorPeliculaRepository actorPeliculaRepository;

	public Reto generarRetoAleatorio() {
		Random rnd = new Random();

		List<Actor> candidatosA = actorRepository.findActoresConMultiplesPeliculas(5L);
		if (candidatosA.isEmpty()) {
			log.warn("No se encontraron actores con más de 5 películas.");
			throw new IllegalStateException("No hay actores suficientes para generar retos");
		}

		while (true) { // Loop hasta encontrar un reto válido
			Actor actorA = candidatosA.get(rnd.nextInt(candidatosA.size()));
			log.debug("Actor A seleccionado: {} (ID: {})", actorA.getNombreCompleto(), actorA.getId());

			// Actor C debe haber trabajado con A y tener al menos 5 películas
			List<Actor> candidatosC = actorRepository.findColaboradores(actorA.getId()).stream()
					.filter(c -> c.getPeliculas().size() >= 5)
					.collect(Collectors.toList());

			if (candidatosC.isEmpty()) {
				log.debug("No se encontraron candidatos C válidos para {}. Reintentando...", actorA.getNombreCompleto());
				continue; // Volver a elegir otro Actor A
			}

			Actor actorC = candidatosC.get(rnd.nextInt(candidatosC.size()));
			log.debug("Actor C elegido (solución): {} (ID: {})", actorC.getNombreCompleto(), actorC.getId());

			// Actor B ha trabajado con C, distinto de A y C, y con al menos 5 películas
			List<Actor> candidatosB = actorRepository.findColaboradores(actorC.getId()).stream()
					.filter(b -> !b.getId().equals(actorA.getId()) && !b.getId().equals(actorC.getId()))
					.filter(b -> b.getPeliculas().size() >= 5)
					.collect(Collectors.toList());

			if (candidatosB.isEmpty()) {
				log.debug("No se encontraron candidatos B válidos para {}. Reintentando...", actorC.getNombreCompleto());
				continue; // Volver a elegir otro Actor A
			}

			for (Actor actorB : candidatosB) {
				log.debug("Probando con Actor B: {} (ID: {})", actorB.getNombreCompleto(), actorB.getId());

				// Películas AC
				List<Pelicula> peliculasAC = actorPeliculaRepository.findPeliculasEnComunExcluyendoColeccion(
						actorA.getId(), actorC.getId(), null);
				if (peliculasAC.isEmpty()) continue;

				Pelicula peliculaAC = peliculasAC.get(rnd.nextInt(peliculasAC.size()));
				Integer coleccionExcluida = peliculaAC.getIdColeccion();
				log.debug("Película AC: {} (Colección: {})", peliculaAC.getTitulo(), coleccionExcluida);

				// Películas BC excluyendo la colección de AC
				List<Pelicula> peliculasBC = actorPeliculaRepository.findPeliculasEnComunExcluyendoColeccion(
						actorB.getId(), actorC.getId(), coleccionExcluida);
				if (peliculasBC.isEmpty()) continue;

				Pelicula peliculaBC = peliculasBC.get(rnd.nextInt(peliculasBC.size()));
				log.debug("Película BC: {} (Colección: {})", peliculaBC.getTitulo(), peliculaBC.getIdColeccion());

				if (!peliculaAC.equals(peliculaBC)) {
					log.info("✅ Reto generado con éxito: [{}] - [{}] - [{}]",
							actorA.getNombreCompleto(),
							actorB.getNombreCompleto(),
							actorC.getNombreCompleto());
					return new Reto(actorA, actorB, actorC, peliculaAC, peliculaBC);
				} else {
					log.debug("Películas AC y BC coinciden, se descartará este intento.");
				}
			}

			log.debug("No se pudo generar un reto válido con Actor A {}. Reintentando...", actorA.getNombreCompleto());
		}
	}

}
