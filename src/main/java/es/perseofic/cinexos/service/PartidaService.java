package es.perseofic.cinexos.service;

import org.springframework.stereotype.Service;

import es.perseofic.cinexos.model.Partida;
import es.perseofic.cinexos.repository.PartidaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartidaService {

	private final PartidaRepository partidaRepository;

	public Partida guardarPartida(Partida partida) {
		return partidaRepository.save(partida);
	}

}
