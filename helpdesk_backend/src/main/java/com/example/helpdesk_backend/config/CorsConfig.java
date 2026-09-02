package com.example.helpdesk_backend.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    /**
     * Origens liberadas. O default cobre o Vite local; em producao aponte
     * CORS_ALLOWED_ORIGINS para o dominio do front hospedado (varias separadas por
     * virgula). Antes a lista era fixa em localhost e o front publicado era bloqueado
     * pelo navegador ao chamar a API.
     */
    @Value("${cors.allowed-origins:http://localhost:[*],http://127.0.0.1:[*]}")
    private String allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(
            Arrays.stream(allowedOrigins.split(","))
                  .map(String::trim)
                  .filter(origem -> !origem.isEmpty())
                  .toList());

        //Métodos HTTP liberados para o FrontEnd
        configuration.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "PATCH",
            "OPTIONS"
        ));

        //Cabeçalhos permitidos nas requisições (incluindo o "Authorization" para o JWT)
        configuration.setAllowedHeaders(List.of("*"));

        //Expõe cabeçalhos de resposta para leitura no FrontEnd(OBS: Se Necessário)
        configuration.setExposedHeaders(List.of(
            "Authorization",
            "Content-Disposition"
        ));

        //Permite o envio de credenciais (cookies/headers de autenticação)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
