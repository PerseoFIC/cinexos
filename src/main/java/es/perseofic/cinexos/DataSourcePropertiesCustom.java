package es.perseofic.cinexos;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourcePropertiesCustom {

	private String url;
	private String username;
	private String password;

}
