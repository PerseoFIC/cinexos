package es.perseofic.cinexos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import es.perseofic.cinexos.model.Partida;
import es.perseofic.cinexos.model.Reto;
import es.perseofic.cinexos.service.PartidaService;
import es.perseofic.cinexos.service.RetoService;
//import es.perseofic.nexosdecine.service.TmdbService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping({"", "/", "/portada"})
public class PortadaController {

//	private final TmdbService tmdbService;
	private final RetoService retoService;
	private final PartidaService partidaService;

	@GetMapping("/")
	public String home(Model model) {
		Reto reto = retoService.generarRetoAleatorio();
		model.addAttribute("reto", reto);
		return "index";
	}

//	@GetMapping("/cargarDatosIniciales")
//	public String cargarDatosIniciales(Model model) {
//		model.addAttribute("mensaje", "Iniciando carga de datos desde TMDb...");
//		tmdbService.cargarDatosIniciales();
//		model.addAttribute("mensaje", "Carga completada con éxito ✅");
//		return "index";
//	}

//	@GetMapping("/actualizarColecciones")
//	public String actualizarColecciones(Model model) {
//		tmdbService.actualizarColecciones();
//		model.addAttribute("mensaje", "Colecciones actualizadas correctamente ✅");
//		return "index";
//	}

	@GetMapping("/generarReto")
	public String generarReto(Model model) {
		Reto r = retoService.generarRetoAleatorio();
		model.addAttribute("actorA", r.getActorA().getNombreCompleto());
		model.addAttribute("actorB", r.getActorB().getNombreCompleto());
		model.addAttribute("actorC", r.getActorC().getNombreCompleto());
		return "index";
	}

	@PostMapping("/guardar")
	public ResponseEntity<Void> guardarPartida(@RequestBody Partida partida) {
		partidaService.guardarPartida(partida);
		return ResponseEntity.noContent().build();
	}

}
