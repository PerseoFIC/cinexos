package es.perseofic.cinexos;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;

@Component
public class DespertarBD {

	private final DataSourcePropertiesCustom properties;

	public DespertarBD(DataSourcePropertiesCustom properties) {
		this.properties = properties;
	}

	@PostConstruct
	public void despertarBD() {
		try {
			System.out.println("Intentando despertar la base de datos...");
			long inicio = System.currentTimeMillis();
			try (Connection conn = DriverManager.getConnection(properties.getUrl(), properties.getUsername(), properties.getPassword())) {
				System.out.println("Conexión de despertar exitosa en " + (System.currentTimeMillis() - inicio) + " ms.");
			}
		} catch (Exception e) {
			System.err.println("No se pudo despertar la BD: " + e.getMessage());
		}
	}

}
