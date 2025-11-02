package es.perseofic.cinexos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DataSourcePropertiesCustom.class)
public class CinexosApplication {

	public static void main(String[] args) {
		SpringApplication.run(CinexosApplication.class, args);
	}

}
