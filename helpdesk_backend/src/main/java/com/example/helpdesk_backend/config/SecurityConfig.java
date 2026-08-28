package com.example.helpdesk_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.helpdesk_backend.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter; //Adicionado Recentemente

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // 1. Ativa a configuração do CORS injetada no parâmetro
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // 2. Desabilita CSRF pois a API é Stateless e usa JWT
            .csrf(csrf -> csrf.disable()) 
            
            // 3. Define o gerenciamento de sessão como Stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 4. Mapeamento de Rotas e Permissões
            .authorizeHttpRequests(authorize -> authorize

                    //Acesso público ap Swagger UI e à documentação OpenAPI
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    // Rota pública de Autenticação
                    .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                    // Gestão de Usuários: Restrita a ADMIN e ATENDENTE
                    .requestMatchers(HttpMethod.POST, "/usuarios").hasAnyRole("ADMIN", "ATENDENTE")
                    .requestMatchers(HttpMethod.GET, "/usuarios").hasAnyRole("ADMIN", "ATENDENTE")
                    .requestMatchers(HttpMethod.PUT, "/usuarios/*").hasAnyRole("ADMIN", "ATENDENTE")
                    .requestMatchers(HttpMethod.DELETE, "/usuarios/*").hasAnyRole("ADMIN", "ATENDENTE")

                    // Trava de Primeiro Acesso e Complemento de Perfil
                    .requestMatchers(HttpMethod.PATCH, "/usuarios/complementar-perfil").authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/usuarios/*/complementar-perfil").hasAnyRole("ADMIN", "ATENDENTE")

                    // Gestão de Chamados
                    .requestMatchers(HttpMethod.POST, "/chamados").authenticated()
                    .requestMatchers(HttpMethod.GET, "/chamados").authenticated()
                    .requestMatchers(HttpMethod.POST, "/chamados/*/escalonar").hasAnyRole("ADMIN", "ATENDENTE")
                    .requestMatchers(HttpMethod.PATCH, "/chamados/*/assumir").hasAnyRole("ADMIN", "ATENDENTE")
                    .requestMatchers(HttpMethod.PATCH, "/chamados/*/status").hasAnyRole("ADMIN", "ATENDENTE")
                    .requestMatchers(HttpMethod.PATCH, "/chamados/*/avaliar").authenticated()

                    // Gestão de Anexos
                    .requestMatchers(HttpMethod.POST, "/chamados/*/anexos").authenticated()
                    .requestMatchers(HttpMethod.GET, "/chamados/*/anexos").authenticated()
                    .requestMatchers(HttpMethod.GET, "/anexos/*/download").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/anexos/*").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/chamados/*/anexos/*").authenticated()

                    //Logs de Escalonamentos
                    .requestMatchers(HttpMethod.GET,"/escalonamentos/**").hasAnyRole("ADMIN", "ATENDENTE")

                    // Restringe qualquer outra requisição para usuários autenticados
                    .anyRequest().authenticated()
            )
            
            // 5. Adiciona o filtro JWT antes do filtro padrão do Spring
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    // Configura o encriptador de senhas para o padrão BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Expõe o AuthenticationManager para podermos usá-lo no AuthService no momento do login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
