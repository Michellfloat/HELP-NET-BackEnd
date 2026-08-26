package com.example.helpdesk_backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();

        //Aceita chamadas de qualquer origem, método e cabeçalho como localhost(Vite: 5173, 3000, etc.), mas em produção isso deve ser restringido.
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:[*]",
            "http://127.0.0.1:[*]"
        ));

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
