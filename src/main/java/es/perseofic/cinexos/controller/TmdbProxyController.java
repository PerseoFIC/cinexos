package es.perseofic.cinexos.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/tmdb")
public class TmdbProxyController {

	@Value("${tmdb.api.key}")
	private String tmdbApiKey;

	private final RestTemplate restTemplate = new RestTemplate();

	@GetMapping("/search/person")
	public ResponseEntity<String> searchPerson(@RequestParam String query) {
		String url = "https://api.themoviedb.org/3/search/person?api_key=" + tmdbApiKey + "&language=es-ES&query=" + query;
		return ResponseEntity.ok(restTemplate.getForObject(url, String.class));
	}

	@GetMapping("/search/movie")
	public ResponseEntity<String> searchMovie(@RequestParam String query) {
		String url = "https://api.themoviedb.org/3/search/movie?api_key=" + tmdbApiKey + "&language=es-ES&query=" + query;
		return ResponseEntity.ok(restTemplate.getForObject(url, String.class));
	}

	@GetMapping("/validate/credit")
	public ResponseEntity<Boolean> validateCredit(@RequestParam Long personId, @RequestParam Long movieId) {
		String url = "https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=" + tmdbApiKey;
		String response = restTemplate.getForObject(url, String.class);
		return ResponseEntity.ok(response != null && response.contains("\"id\":" + personId + ","));
	}

}
