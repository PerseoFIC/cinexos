const Stats = (function () {
	const STORAGE_KEY = 'cinexos_stats';

	const defaultStats = {
		partidasJugadas: 0,
		partidasVictoriaTotal: 0,
		partidasVictoriaUnaPista: 0,
		partidasVictoriaDosPistas: 0,
		partidasFracaso: 0,
		partidasAbandonadas: 0
	};

	function load() {
		const data = localStorage.getItem(STORAGE_KEY);
		if (data) {
			try {
				return JSON.parse(data);
			} catch {
				localStorage.removeItem(STORAGE_KEY);
			}
		}
		save({ ...defaultStats });
		return { ...defaultStats };
	}

	function save(stats) {
		localStorage.setItem(STORAGE_KEY, JSON.stringify(stats));
	}

	// Llamar al iniciar un reto
	function startReto() {
		const stats = load();
		stats.partidasJugadas++;
		stats.partidasAbandonadas++;
		save(stats);
		return stats;
	}

	// Llamar al finalizar un reto, con éxito o fracaso
	// outcome: 'victoria' | 'fracaso'
	// pistasUsed: 0, 1 o 2
	function finishReto(outcome, pistasUsed) {
		const stats = load();
		if (stats.partidasAbandonadas > 0) stats.partidasAbandonadas--;

		if (outcome === 'victoria') {
			if (pistasUsed === 0) stats.partidasVictoriaTotal++;
			else if (pistasUsed === 1) stats.partidasVictoriaUnaPista++;
			else if (pistasUsed === 2) stats.partidasVictoriaDosPistas++;
		} else if (outcome === 'fracaso') {
			stats.partidasFracaso++;
		}

		save(stats);
		return stats;
	}

	function reset() {
		save({ ...defaultStats });
		return load();
	}

	return {
		load,
		save,
		startReto,
		finishReto,
		reset
	};
})();

document.addEventListener('DOMContentLoaded', () => {
	const statsIcon = document.getElementById('statsIcon');
	const statsPopup = document.getElementById('statsPopup');
	const cerrarStats = document.getElementById('cerrarStats');

	if (statsIcon && statsPopup) {
		statsIcon.addEventListener('click', () => {
		const stats = Stats.load();
			document.getElementById('statPartidasJugadas').textContent = stats.partidasJugadas;
			document.getElementById('statVictoriaTotal').textContent = stats.partidasVictoriaTotal;
			document.getElementById('statVictoriaUnaPista').textContent = stats.partidasVictoriaUnaPista;
			document.getElementById('statVictoriaDosPistas').textContent = stats.partidasVictoriaDosPistas;
			document.getElementById('statFracaso').textContent = stats.partidasFracaso;
			document.getElementById('statAbandonadas').textContent = stats.partidasAbandonadas;

			statsPopup.style.display = 'flex';
		});

		cerrarStats.addEventListener('click', () => {
			statsPopup.style.display = 'none';
		});

		statsPopup.addEventListener('click', e => {
			if (e.target === statsPopup) statsPopup.style.display = 'none';
		});
	}
});
