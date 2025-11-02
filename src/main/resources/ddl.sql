-- ============================================================
-- Base de datos: cinexos
-- Estructura para películas, actores y relaciones
-- ============================================================

CREATE DATABASE IF NOT EXISTS `cinexos` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `cinexos`;

-- ============================================================
-- Tabla: pelicula
-- ============================================================
CREATE TABLE `pelicula` (
  `id` INT(10) AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador interno',
  `id_tmdb` INT(10) COMMENT 'ID original de TMDb',
  `titulo` VARCHAR(255) COMMENT 'Título en español',
  `titulo_original` VARCHAR(255) COMMENT 'Título original',
  `sinopsis` VARCHAR(4000) COMMENT 'Resumen de la película (truncado a 4000)',
  `fecha_estreno` DATE COMMENT 'Fecha de estreno',
  `idioma_original` VARCHAR(10) COMMENT 'Código ISO idioma original',
  `nota_media` DOUBLE COMMENT 'Valor medio de votos',
  `numero_votos` INT(10) COMMENT 'Número total de votos',
  `ruta_poster` VARCHAR(255) COMMENT 'Ruta relativa del póster',
  `id_coleccion` INT(10) NULL COMMENT 'ID de la colección en TMDb',
  `nombre_coleccion` VARCHAR(255) NULL COMMENT 'Nombre de la colección'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================================
-- Tabla: actor
-- ============================================================
CREATE TABLE `actor` (
  `id` INT(10) AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador interno',
  `id_tmdb` INT(10) COMMENT 'ID original de TMDb',
  `nombre_completo` VARCHAR(255) COMMENT 'Nombre completo del actor',
  `fecha_nacimiento` DATE COMMENT 'Fecha de nacimiento',
  `lugar_nacimiento` VARCHAR(255) COMMENT 'Lugar de nacimiento',
  `ruta_foto` VARCHAR(255) COMMENT 'Ruta relativa a la foto'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================================
-- Tabla: actor_pelicula
-- ============================================================
CREATE TABLE `actor_pelicula` (
  `id` INT(10) AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador interno',
  `id_actor` INT(10) COMMENT 'Referencia al actor',
  `id_pelicula` INT(10) COMMENT 'Referencia a la película',
  `personaje` VARCHAR(255) COMMENT 'Nombre del personaje interpretado',
  `orden_creditos` INT(10) COMMENT 'Orden en los créditos',
  CONSTRAINT `fk_actor_pelicula_actor` FOREIGN KEY (`id_actor`) REFERENCES `actor`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_actor_pelicula_pelicula` FOREIGN KEY (`id_pelicula`) REFERENCES `pelicula`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================================
-- Tabla: partida
-- ============================================================
CREATE TABLE `partida` (
  `id` INT(10) AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador de la partida',
  
  -- FKs a los elementos del reto
  `id_actor_a` INT(10) NOT NULL,
  `id_actor_b` INT(10) NOT NULL,
  `id_actor_c` INT(10) NOT NULL,
  `id_pelicula_ac` INT(10) NOT NULL,
  `id_pelicula_bc` INT(10) NOT NULL,

  -- Último intento para cada reto
  `intento_nombre_actor_c` VARCHAR(255) NOT NULL,
  `intento_titulo_pelicula_ac` VARCHAR(255) NOT NULL,
  `intento_titulo_pelicula_bc` VARCHAR(255) NOT NULL,

  -- Resultado de la partida
  `exito` BOOLEAN NOT NULL,
  `pistas_usadas` INT(1) NOT NULL,

  CONSTRAINT `fk_partida_actor_a` FOREIGN KEY (`id_actor_a`) REFERENCES `actor`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_partida_actor_b` FOREIGN KEY (`id_actor_b`) REFERENCES `actor`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_partida_actor_c` FOREIGN KEY (`id_actor_c`) REFERENCES `actor`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_partida_pelicula_ac` FOREIGN KEY (`id_pelicula_ac`) REFERENCES `pelicula`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_partida_pelicula_bc` FOREIGN KEY (`id_pelicula_bc`) REFERENCES `pelicula`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================================================
-- Índices opcionales
-- ============================================================
CREATE INDEX `idx_pelicula_id_tmdb` ON `pelicula`(`id_tmdb`);
CREATE INDEX `idx_actor_id_tmdb` ON `actor`(`id_tmdb`);
CREATE INDEX `idx_actor_pelicula_actor` ON `actor_pelicula`(`id_actor`);
CREATE INDEX `idx_actor_pelicula_pelicula` ON `actor_pelicula`(`id_pelicula`);
