package com.example.helpdesk_backend.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import com.example.helpdesk_backend.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class JwtService {

    // Valor publicamente conhecido (esta no repositorio): serve para o dia a dia de
    // desenvolvimento e e recusado em producao por validarConfiguracao().
    private static final String SEGREDO_PADRAO = "helpdesk_secret_key_32_bytes_min_length_for_hmac_sha";

    //Chave secreta definida no application.properties ou valor default para o Desenvolvimento
    @Value("${api.security.token.secret:helpdesk_secret_key_32_bytes_min_length_for_hmac_sha}")
    private String secretKey;

    private final Environment environment;

    public JwtService(Environment environment) {
        this.environment = environment;
    }

    /**
     * Falha o boot em producao se o segredo nao tiver sido trocado. Antes o default
     * ficava valendo em silencio, e qualquer pessoa com acesso ao repositorio podia
     * forjar um token de ADMIN valido no ambiente publicado.
     */
    @PostConstruct
    void validarConfiguracao() {
        boolean producao = environment.matchesProfiles("prod");

        if (producao && SEGREDO_PADRAO.equals(secretKey)) {
            throw new IllegalStateException(
                    "JWT_SECRET nao configurado: o profile prod nao pode subir com o segredo padrao do repositorio.");
        }

        if (secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET precisa ter ao menos 32 bytes para HMAC-SHA256.");
        }

        if (!producao && SEGREDO_PADRAO.equals(secretKey)) {
            log.warn("Usando o segredo JWT padrao de desenvolvimento. Defina JWT_SECRET antes de publicar.");
        }
    }

    @Value("${api.security.token.expiration:86400000}") //86400000 = 24 horas em milisegundos
    private long expirationTime;

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(Usuario usuario){
        return Jwts.builder()
        .subject(usuario.getEmail())
        .claim("id", usuario.getId())
        .claim("nome", usuario.getNome())
        .claim("perfil", usuario.getPerfil().name())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(getSigningKey())
        .compact();
    }

    public String validarETrazerSubject(String token){
        try{
            Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();

            return claims.getSubject(); //Irá retornar o E-mail
        } catch (ExpiredJwtException e){
            //Agora: Se espera que o usuário só precise entrar de novo.
            log.debug("Token expirado para {}", e.getClaims().getSubject());
            return null; //Token inválido/expirado
        } catch (JwtException e){
            //Inesperado: Assinatura, formato ou chave Token
            log.warn("Token Rejeitado: {}",e.getMessage());

            return null;//Token inválido/rejeitado
        }
        
    }
}
