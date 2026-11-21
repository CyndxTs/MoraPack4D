/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       MorapackApplication.java
 [**/

package com.pucp.dp1.grupo4d.morapack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class MorapackApplication {

	public static void main(String[] args) {
		// Todo el sistema trabaja en UTC (el front convierte a la hora local)
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		List<String> argumentos = new ArrayList<>(Arrays.asList(args));
		if (argumentos.contains("--seed")) {
			// Modo seed: reinicia la BD con los scripts de db/seed y termina.
			// Puerto aleatorio para no chocar con un server que ya esté corriendo.
			argumentos.add("--morapack.seed.enabled=true");
			argumentos.add("--server.port=0");
		}
		SpringApplication.run(MorapackApplication.class, argumentos.toArray(new String[0]));
	}
}
