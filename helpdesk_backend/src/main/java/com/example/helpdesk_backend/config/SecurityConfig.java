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
                        //1-Rota pública de autenticação (login) que não requer autenticação
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                        // 2. Criação de Usuários: Apenas Atendentes (RF04)
                        .requestMatchers(HttpMethod.POST, "/usuarios").hasRole("ATENDENTE")
                        .requestMatchers(HttpMethod.GET, "/usuarios").hasRole("ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/usuarios/*").hasRole("ATENDENTE")

                        // 3. Edição/Listagem de Usuários
                        .requestMatchers(HttpMethod.PATCH, "/usuarios/complementar-perfil").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/usuarios/*").hasRole("ATENDENTE")
                        // 4. Complemento de Perfil (Primeiro Acesso) - Aberto para qualquer usuário autenticado
                        .requestMatchers(HttpMethod.GET, "/chamados").hasRole("ATENDENTE")
                        .requestMatchers(HttpMethod.POST, "/chamados/*/escalonar").hasRole("ATENDENTE")

                        // 5. Chamados e Anexos - Qualquer usuário autenticado
                        .requestMatchers("/chamados/*/anexos").authenticated()
                        .requestMatchers("/anexos/*/download").authenticated()
                        
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
