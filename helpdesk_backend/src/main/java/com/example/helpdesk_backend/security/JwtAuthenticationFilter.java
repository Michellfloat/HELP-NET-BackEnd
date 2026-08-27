package com.example.helpdesk_backend.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = recuperarToken(request);

        if (token != null) {
            String email = jwtService.validarETrazerSubject(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

                if (usuario != null) {
                    String perfilNome = usuario.getPerfil().name();
                    
                    // ATUALIZADO: Registra o perfil mapeando a autoridade bruta e com o prefixo 'ROLE_'
                    // Permite o funcionamento de .hasRole("ADMIN") e .hasAuthority("ADMIN") no SecurityConfig
                    var authorities = List.of(
                        new SimpleGrantedAuthority(perfilNome),
                        new SimpleGrantedAuthority("ROLE_" + perfilNome)
                    );

                    var authentication = new UsernamePasswordAuthenticationToken(usuario.getEmail(), null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7); // Remove "Bearer " (com o espaço) corretamente
    }
    
}
