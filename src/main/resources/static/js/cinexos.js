document.addEventListener('DOMContentLoaded', () => {
	const delay = 500;
	const actorInput = document.getElementById('actorCInput');
	const peliculaACInput = document.getElementById('peliculaACInput');
	const peliculaBCInput = document.getElementById('peliculaBCInput');
	const resolverBtn = document.getElementById('resolverBtn');
	const pistaACBtn = document.getElementById('pistaACBtn');
	const pistaBCBtn = document.getElementById('pistaBCBtn');
	const nuevaPartidaIcon = document.getElementById('nuevaPartidaIcon');

	if (nuevaPartidaIcon) {
		nuevaPartidaIcon.addEventListener('click', () => {
			// Redirige a la misma página para generar un nuevo reto
			window.location.reload();
		});
	}

	let seleccion = {
		actorC: null,
		peliculaAC: null,
		peliculaBC: null
	};

	let intentos = 0;
	const maxIntentos = 3;

	// --- Inicializa estadísticas al empezar el reto ---
	if (typeof Stats !== 'undefined') {
		Stats.startReto();
	}

	// --- Funciones de búsqueda ---
	function setupSearch(input, type, placeholderId, sugerenciasId, key) {
		let timeout = null;
		input.addEventListener('input', () => {
			clearTimeout(timeout);
			const query = input.value.trim();
			if (query.length < 2) {
				document.getElementById(sugerenciasId).innerHTML = '';
				return;
			}
			timeout = setTimeout(() => fetchSuggestions(type, query, sugerenciasId, input, placeholderId, key), delay);
		});
	}

	function fetchSuggestions(type, query, sugerenciasId, input, placeholderId, key) {
		fetch(`/api/tmdb/search/${type}?query=${encodeURIComponent(query)}`)
			.then(r => r.json())
			.then(data => {
				const container = document.getElementById(sugerenciasId);
				container.innerHTML = '';

				const results = data.results || [];
				if (results.length === 0) return;

				results.sort((a, b) => (type === 'person' ? b.popularity - a.popularity : b.vote_count - a.vote_count));
				results.slice(0, 10).forEach(item => {
					const div = document.createElement('div');
					if (type === 'person') {
						const conocidas = (item.known_for || [])
							.slice(0, 3)
							.map(p => p.title || p.name)
							.join(', ');
						div.textContent = `${item.name}${conocidas ? ' (' + conocidas + ')' : ''}`;
					} else {
						const year = item.release_date ? `[${item.release_date.substring(0, 4)}]` : '';
						div.textContent = `${item.title} ${year}`;
					}

					div.addEventListener('click', () => {
						container.innerHTML = '';
						input.value = type === 'person' ? item.name : item.title;

						if (type === 'person') {
							seleccion[key] = {
								idTmdb: item.id,
								nombre: item.name,
								rutaFoto: item.profile_path ? `https://image.tmdb.org/t/p/w185${item.profile_path}` : null
							};
						} else {
							seleccion[key] = {
								idTmdb: item.id,
								titulo: item.title,
								rutaPoster: item.poster_path ? `https://image.tmdb.org/t/p/w300${item.poster_path}` : null
							};
						}

						const imgUrl = type === 'person' ? seleccion[key].rutaFoto : seleccion[key].rutaPoster;
						const placeholder = document.getElementById(placeholderId);
						if (!placeholder) return;

						const wrapper = placeholder.closest('.input-wrapper');
						if (wrapper) {
							const existingVisual = wrapper.querySelector('img, .placeholder');
							if (existingVisual) existingVisual.remove();
						}

						if (imgUrl) {
							const img = document.createElement('img');
							img.src = imgUrl;
							img.alt = input.value;
							img.width = placeholder.offsetWidth || 120;
							img.height = placeholder.offsetHeight || 180;
							img.style.borderRadius = '10px';
							img.style.objectFit = 'cover';
							img.id = placeholderId;
							wrapper.prepend(img);
						} else {
							const nuevoPlaceholder = document.createElement('div');
							nuevoPlaceholder.className = type === 'person' ? 'placeholder actor' : 'placeholder poster';
							nuevoPlaceholder.id = placeholderId;
							nuevoPlaceholder.textContent = type === 'person' ? '🕵️' : '🎞️';
							wrapper.prepend(nuevoPlaceholder);
						}

						checkResolverStatus();
					});

					container.appendChild(div);
				});
			})
			.catch(err => console.error(`❌ Error buscando ${type}:`, err));
	}

	// --- Inicializar ---
	setupSearch(actorInput, 'person', 'actorCPlaceholder', 'actorCSugerencias', 'actorC');
	setupSearch(peliculaACInput, 'movie', 'peliculaACPlaceholder', 'peliculaACSugerencias', 'peliculaAC');
	setupSearch(peliculaBCInput, 'movie', 'peliculaBCPlaceholder', 'peliculaBCSugerencias', 'peliculaBC');

	resolverBtn.disabled = true;
	resolverBtn.addEventListener('click', resolverReto);

	pistaACBtn.addEventListener('click', () => revelarPelicula('AC'));
	pistaBCBtn.addEventListener('click', () => revelarPelicula('BC'));

	function revelarPelicula(tipo) {
		const wrapper = document.querySelector(`#pelicula${tipo}Placeholder`).closest('.input-wrapper');
		const peliculaId = wrapper.dataset.peliculaIdTmdb;
		const peliculaTitulo = wrapper.dataset.peliculaTitulo;
		const peliculaPoster = wrapper.dataset.peliculaPoster;
		const placeholder = document.getElementById(`pelicula${tipo}Placeholder`);
		const input = document.getElementById(`pelicula${tipo}Input`);
		const sugerencias = document.getElementById(`pelicula${tipo}Sugerencias`);
		const pistaBtn = document.getElementById(`pista${tipo}Btn`);

		if (!peliculaId || !peliculaTitulo) return;

		if (peliculaPoster) {
			const img = document.createElement('img');
			img.src = `https://image.tmdb.org/t/p/w185${peliculaPoster}`;
			img.alt = peliculaTitulo;
			img.width = placeholder.offsetWidth;
			img.height = placeholder.offsetHeight;
			img.style.borderRadius = '10px';
			img.style.objectFit = 'cover';
			placeholder.replaceWith(img);
			img.id = `pelicula${tipo}Placeholder`;
		}

		const label = document.createElement('p');
		label.classList.add('actor-nombre');
		label.textContent = peliculaTitulo;
		if (sugerencias) sugerencias.remove();
		if (input) input.replaceWith(label);

		pistaBtn.textContent = '🔍 Pista usada';
		pistaBtn.classList.add('usada');
		pistaBtn.disabled = true;

		seleccion[`pelicula${tipo}`] = {
			idTmdb: peliculaId,
			titulo: peliculaTitulo,
			rutaPoster: peliculaPoster ? `https://image.tmdb.org/t/p/w300${peliculaPoster}` : null,
			pistaUsada: true
		};

		checkResolverStatus();
	}

	function checkResolverStatus() {
		const ready = !!(seleccion.actorC?.idTmdb && seleccion.peliculaAC?.idTmdb && seleccion.peliculaBC?.idTmdb) && intentos < maxIntentos;
		resolverBtn.disabled = !ready;
	}

	function resolverReto() {
		if (!seleccion.actorC?.idTmdb || !seleccion.peliculaAC?.idTmdb || !seleccion.peliculaBC?.idTmdb) return;

		intentos++;
		const intentNumber = intentos;

		const actorAEl = document.querySelector('[data-actor-a-id-tmdb]');
		const actorA = { idTmdb: actorAEl?.dataset.actorAIdTmdb, nombre: actorAEl?.querySelector('.actor-nombre')?.textContent || 'Desconocido' };
		const actorBEl = document.querySelector('[data-actor-b-id-tmdb]');
		const actorB = { idTmdb: actorBEl?.dataset.actorBIdTmdb, nombre: actorBEl?.querySelector('.actor-nombre')?.textContent || 'Desconocido' };

		const relaciones = [
			{ actor: actorA, pelicula: seleccion.peliculaAC, id: 'relacionA-AC' },
			{ actor: seleccion.actorC, pelicula: seleccion.peliculaAC, id: 'relacionAC-C' },
			{ actor: seleccion.actorC, pelicula: seleccion.peliculaBC, id: 'relacionC-BC' },
			{ actor: actorB, pelicula: seleccion.peliculaBC, id: 'relacionBC-B' }
		];

		console.group(`🔍 Intento ${intentos} - Relaciones a evaluar`);
		relaciones.forEach(r => {
			console.log(`• ${r.id} → Actor ${r.actor.idTmdb} (${r.actor.nombre}) con Película ${r.pelicula.idTmdb} (${r.pelicula.titulo})`);
		});
		console.groupEnd();

		const resultadosIntento = [];
		let fetchesPendientes = relaciones.length;

		relaciones.forEach((r, idx) => {
			if ((r.id === 'relacionA-AC' && seleccion.peliculaAC.pistaUsada) ||
				(r.id === 'relacionBC-B' && seleccion.peliculaBC.pistaUsada)) {
				const icon = document.getElementById(r.id);
				if (icon) {
					icon.textContent = '🔍';
					icon.style.color = '#b71c1c';
				}
				resultadosIntento[idx] = '🔍';
				fetchesPendientes--;
				if (fetchesPendientes === 0) actualizarCuadroResultados(intentNumber, resultadosIntento);
				return;
			}

			const url = `/api/tmdb/validate/credit?personId=${r.actor.idTmdb}&movieId=${r.pelicula.idTmdb}`;
			console.log(`🌐 Llamando a: ${url}`);
			fetch(url)
				.then(resp => resp.json())
				.then(valido => {
					const icon = document.getElementById(r.id);
					if (icon) {
						icon.textContent = valido ? '✅' : '❌';
						icon.style.color = valido ? '#4caf50' : '#e53935';
					}
					resultadosIntento[idx] = valido ? '✅' : '❌';
					fetchesPendientes--;
					if (fetchesPendientes === 0) {
						console.log(`✅ Resultados intento ${intentos}:`, resultadosIntento);
						actualizarCuadroResultados(intentNumber, resultadosIntento);
					}
				})
				.catch(err => {
					console.error(`❌ Error validando relación ${r.id}:`, err);
					resultadosIntento[idx] = '❌';
					fetchesPendientes--;
					if (fetchesPendientes === 0) actualizarCuadroResultados(intentNumber, resultadosIntento);
				});
		});
	}

	function actualizarCuadroResultados(intentNumber, resultados) {
		const tabla = document.getElementById('resultadosTable');
		if (!tabla) return;

		const filas = tabla.querySelectorAll('tbody tr');
		if (intentNumber > filas.length) return;
		const fila = filas[intentNumber - 1];
		if (!fila) return;

		const celdas = fila.querySelectorAll('td');
		const tdPeliculaAC = celdas[1];
		const tdActorC = celdas[2];
		const tdPeliculaBC = celdas[3];
		const tdAciertos = celdas[4];

		tdPeliculaAC.textContent = seleccion.peliculaAC.titulo;
		tdActorC.textContent = seleccion.actorC.nombre;
		tdPeliculaBC.textContent = seleccion.peliculaBC.titulo;

		if (seleccion.peliculaAC.pistaUsada) {
			tdPeliculaAC.style.backgroundColor = '#800000';
			tdPeliculaAC.style.color = '#ddd';
		}
		if (seleccion.peliculaBC.pistaUsada) {
			tdPeliculaBC.style.backgroundColor = '#800000';
			tdPeliculaBC.style.color = '#ddd';
		}

		tdAciertos.textContent = '';
		resultados.forEach(v => {
			const span = document.createElement('span');
			span.textContent = v;
			span.style.marginRight = '0.3rem';
			if (v === '🔍') span.style.color = '#b71c1c';
			tdAciertos.appendChild(span);
		});

		const todasExitosOPista = resultados.every(v => v === '✅' || v === '🔍');
		const haFallado = resultados.some(v => v === '❌');

		if (todasExitosOPista || intentos >= maxIntentos) {
			resolverBtn.disabled = true;
		} else {
			checkResolverStatus();
		}

		if (todasExitosOPista) {
			const pistasUsadas = ['peliculaAC', 'peliculaBC'].filter(p => seleccion[p]?.pistaUsada).length;
			if (typeof Stats !== 'undefined') {
				Stats.finishReto('victoria', pistasUsadas);
			}
			guardarPartidaEnBD(true);
			mostrarPopup('¡Enhorabuena! Has encontrado el nexo.');
		} else if (intentos >= maxIntentos && haFallado) {
			if (typeof Stats !== 'undefined') {
				const pistasUsadas = ['peliculaAC', 'peliculaBC'].filter(p => seleccion[p]?.pistaUsada).length;
				Stats.finishReto('fracaso', pistasUsadas);
			}
			guardarPartidaEnBD(false);
			mostrarPopup('No has logrado encontrar el nexo.');

			const actorCOriginalEl = document.querySelector('.actor.input-wrapper[data-actor-id-tmdb]');
			const actorCorrectoNombre = actorCOriginalEl?.dataset.actorNombre || 'Actor desconocido';
			const peliculas = document.querySelectorAll('.pelicula[data-pelicula-id-tmdb]');
			const peliculaACTitulo = peliculas[0]?.dataset.peliculaTitulo || 'Película AC';
			const peliculaBCTitulo = peliculas[1]?.dataset.peliculaTitulo || 'Película BC';

			const tbody = tabla.querySelector('tbody');
			const filaFinal = document.createElement('tr');
			filaFinal.innerHTML = `
				<td>Solución</td>
				<td>${peliculaACTitulo}</td>
				<td>${actorCorrectoNombre}</td>
				<td>${peliculaBCTitulo}</td>
				<td style="color:#4caf50"></td>`;
			tbody.appendChild(filaFinal);
		}
	}

	// --- Después de mostrar popup victoria o fracaso ---
	function guardarPartidaEnBD(exito) {
		const pistasUsadas = ['peliculaAC', 'peliculaBC'].filter(p => seleccion[p]?.pistaUsada).length;

		const actorAEl = document.querySelector('[data-actor-a-id-bd]');
		const actorBEl = document.querySelector('[data-actor-b-id-bd]');
		const actorCEl = document.querySelector('[data-actor-c-id-bd]');
		const peliculaACEl = document.querySelector('[data-pelicula-ac-id-bd]');
		const peliculaBCEl = document.querySelector('[data-pelicula-bc-id-bd]');

		const actorA = { id: parseInt(actorAEl?.dataset.actorAIdBd || 0) };
		const actorB = { id: parseInt(actorBEl?.dataset.actorBIdBd || 0) };
		const actorC = { id: parseInt(actorCEl?.dataset.actorCIdBd || 0) };
		const peliculaAC = { id: parseInt(peliculaACEl?.dataset.peliculaAcIdBd || 0) };
		const peliculaBC = { id: parseInt(peliculaBCEl?.dataset.peliculaBcIdBd || 0) };

		const partida = {
			actorA, actorB, actorC,
			peliculaAC, peliculaBC,
			intentoNombreActorC: seleccion.actorC.nombre,
			intentoTituloPeliculaAC: seleccion.peliculaAC.titulo,
			intentoTituloPeliculaBC: seleccion.peliculaBC.titulo,
			exito,
			pistasUsadas
		};

		fetch('/portada/guardar', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(partida)
		})
		.then(() => console.log('✅ Partida guardada en BD'))
		.catch(err => console.error('❌ Error guardando partida en BD:', err));
	}

	function mostrarPopup(mensaje) {
		const popup = document.createElement('div');
		popup.classList.add('popup-mensaje');
		popup.textContent = mensaje;
		document.body.appendChild(popup);
		setTimeout(() => popup.remove(), 4000);
	}

	// --- Popup de Ayuda ---
	const ayudaIcon = document.getElementById('ayudaIcon');
	const ayudaPopup = document.getElementById('ayudaPopup');
	const cerrarAyuda = document.getElementById('cerrarAyuda');

	if (ayudaIcon && ayudaPopup && cerrarAyuda) {
		ayudaIcon.addEventListener('click', () => ayudaPopup.style.display = 'flex');
		cerrarAyuda.addEventListener('click', () => ayudaPopup.style.display = 'none');
		ayudaPopup.addEventListener('click', e => {
			if (e.target === ayudaPopup) ayudaPopup.style.display = 'none';
		});
	}

	// --- Popup TMDB ---
	const tmdbIcon = document.getElementById('tmdbIcon');
	const tmdbPopup = document.getElementById('tmdbPopup');
	const cerrarTmdb = document.getElementById('cerrarTmdb');
	
	if (tmdbIcon && tmdbPopup && cerrarTmdb) {
		tmdbIcon.addEventListener('click', () => tmdbPopup.style.display = 'flex');
		cerrarTmdb.addEventListener('click', () => tmdbPopup.style.display = 'none');
		tmdbPopup.addEventListener('click', e => {
			if (e.target === tmdbPopup) tmdbPopup.style.display = 'none';
		});
	}
});
