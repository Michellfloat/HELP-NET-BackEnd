package com.example.helpdesk_backend.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.example.helpdesk_backend.exception.StandardError;
import com.example.helpdesk_backend.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

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

                        // Acesso público ap Swagger UI e à documentação OpenAPI
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Rota pública de Autenticação
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()

                        // Usuários: Permite que qualquer usuário autenticado consulte seus próprios
                        // dados
                        .requestMatchers(HttpMethod.GET, "/usuarios/me").authenticated()

                        // Gestão de Usuários: Restrita a ADMIN e ATENDENTE
                        .requestMatchers(HttpMethod.POST, "/usuarios").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.GET, "/usuarios").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/usuarios/*").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.DELETE, "/usuarios/*").hasAnyRole("ADMIN", "ATENDENTE")

                        // Usuários: Troca da própria senha (Qualquer autenticado)
                        .requestMatchers(HttpMethod.PATCH, "/usuarios/me/senha").authenticated()

                        // Usuários: Redefinição de senha por Admin (Apenas ADMIN)
                        .requestMatchers(HttpMethod.PATCH, "/usuarios/*/senha").hasRole("ADMIN")

                        // Gestão de Equipamentos
                        .requestMatchers(HttpMethod.GET, "/equipamentos").authenticated() // Usuários precisam listar
                                                                                          // para vincular ao chamado
                        .requestMatchers(HttpMethod.POST, "/equipamentos").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/equipamentos/*").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.DELETE, "/equipamentos/*").hasAnyRole("ADMIN", "ATENDENTE")

                        // Gestão de Chamados
                        .requestMatchers(HttpMethod.POST, "/chamados").authenticated()
                        .requestMatchers(HttpMethod.GET, "/chamados").authenticated()
                        .requestMatchers(HttpMethod.GET, "/chamados/fila").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.GET, "/chamados/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/chamados/*/escalonar").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.PATCH, "/chamados/*/assumir").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.PATCH, "/chamados/*/status").hasAnyRole("ADMIN", "ATENDENTE")
                        .requestMatchers(HttpMethod.PATCH, "/chamados/*/avaliar").authenticated()

                        // Gestão de Mensagens do Mini Chat
                        .requestMatchers(HttpMethod.GET, "/chamados/*/mensagens").authenticated()
                        .requestMatchers(HttpMethod.POST, "/chamados/*/mensagens").authenticated()

                        // Trilha de histórico do atendimento.
                        // A leitura é de qualquer autenticado porque o solicitante acompanha o
                        // próprio chamado; quem realmente filtra é o serviço, pela mesma regra de
                        // alcance do chamado. Já a escrita é do suporte: a trilha registra o
                        // ATENDIMENTO, e o solicitante fala pela conversa do chamado.
                        .requestMatchers(HttpMethod.GET, "/chamados/*/historico").authenticated()
                        .requestMatchers(HttpMethod.POST, "/chamados/*/historico").hasAnyRole("ADMIN", "ATENDENTE")

                        // Gestão de Anexos
                        .requestMatchers(HttpMethod.POST, "/chamados/*/anexos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/chamados/*/anexos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/anexos/*/download").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/anexos/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/chamados/*/anexos/*").authenticated()

                        // Logs de Escalonamentos: Apenas para ADMINs e ATENDENTEs
                        .requestMatchers(HttpMethod.GET, "/escalonamentos/**").hasAnyRole("ADMIN", "ATENDENTE")

                        // Restringe qualquer outra requisição para usuários autenticados
                        .anyRequest().authenticated())

                // 5. Handlers para tratamento personalizado de erros de autenticação e
                // autorização
                .exceptionHandling(ex -> ex
                        // 401: "Não sei quem você é (sem token, token inválido ou expirado) -> Não
                        // Autorizado
                        .authenticationEntryPoint((req, res, e) -> escreverErro(
                                res, HttpStatus.UNAUTHORIZED,
                                "Sessão expirada ou token inválido", req.getRequestURI()))

                        // 403: Sei quem você é, mas não pode passar pois seu perfil não possui
                        // permissão -> Proibido
                        .accessDeniedHandler((req, res, e) -> escreverErro(
                                res, HttpStatus.FORBIDDEN,
                                "Seu perfil não tem permissão para acessar este recurso.", req.getRequestURI())))

                // 6. Adiciona o filtro JWT antes do filtro padrão do Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // Configura o encriptador de senhas para o padrão BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Expõe o AuthenticationManager para podermos usá-lo no AuthService no momento
    // do login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Aqui a criação do Método para o erro. Segue o mesmo formato do
    // GlobalExceptionHandlerm para o contrato não ter exceção
    private void escreverErro(HttpServletResponse res, HttpStatus status, String message, String path)
            throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(res.getWriter(), new StandardError(LocalDateTime.now(), status.value(), message, path));
    }
}
