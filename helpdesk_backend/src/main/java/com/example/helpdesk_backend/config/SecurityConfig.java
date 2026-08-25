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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; //Adicionado Recentemente

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Desabilita CSRF pois usaremos tokens JWT (stateless)
                .csrf(csrf -> csrf.disable()) 
                // Define o gerenciamento de sessão como Stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Libera apenas o endpoint de login
                        // 1. Rota pública de Autenticação
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                        // 2. Gestão de Usuários: Restrita a ADMIN e ATENDENTE
                        .requestMatchers(HttpMethod.POST, "/usuarios").hasAnyRole("ADMIN", "ATENDENTE", "ROLE_ADMIN", "ROLE_ATENDENTE")
                        .requestMatchers(HttpMethod.GET, "/usuarios").hasAnyRole("ADMIN", "ATENDENTE", "ROLE_ADMIN", "ROLE_ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/usuarios/*").hasAnyRole("ADMIN", "ATENDENTE","ROLE_ADMIN","ROLE_ATENDENTE")
                        .requestMatchers(HttpMethod.DELETE, "/usuarios/*").hasAnyRole("ADMIN", "ATENDENTE","ROLE_ADMIN","ROLE_ATENDENTE") // ADICIONADO: Exclusão de Usuários

                        // 3. Trava de Primeiro Acesso e Complemento de Perfil
                        .requestMatchers(HttpMethod.PATCH, "/usuarios/complementar-perfil").authenticated() // Próprio Usuário Logado
                        .requestMatchers(HttpMethod.PATCH, "/usuarios/*/complementar-perfil").hasAnyRole("ADMIN", "ATENDENTE","ROLE_ADMIN","ROLE_ATENDENTE") // ADICIONADO: Complemento por ID

                        // 4. Gestão de Chamados
                        .requestMatchers(HttpMethod.POST, "/chamados").authenticated() // Abertura Própria ou Proxy
                        .requestMatchers(HttpMethod.GET, "/chamados").authenticated() // Listagem de Fila/Chamados
                        .requestMatchers(HttpMethod.POST, "/chamados/*/escalonar").hasAnyRole("ADMIN", "ATENDENTE","ROLE_ADMIN","ROLE_ATENDENTE") // Escalonamento Restrito

                        // 5. Gestão de Anexos
                        .requestMatchers(HttpMethod.POST, "/chamados/*/anexos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/chamados/*/anexos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/anexos/*/download").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/anexos/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/chamados/*/anexos/*").authenticated()

                        // Restringe qualquer outra requisição para usuários autenticados
                        .anyRequest().authenticated()
                )
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
