/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       WebConfig.java
 [**/

package com.pucp.dp1.grupo4d.morapack;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.Arrays;

@Configuration
public class WebConfig {

    // CORS_ORIGINS: "http://localhost:5173" en desarrollo; la URL del front en Render en producción (separadas por comas)
    @Value("${morapack.cors.origenes:http://localhost:5173}")
    private String origenes;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        String[] permitidos = Arrays.stream(origenes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(permitidos)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Content-Disposition")
                        .allowCredentials(true);
            }
        };
    }
}
