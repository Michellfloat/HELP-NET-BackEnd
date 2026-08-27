package com.example.helpdesk_backend.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.helpdesk_backend.model.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;



@Service
public class JwtService {

    //Chave secreta definida no application.properties ou valor default para o Desenvolvimento
    @Value("${api.security.token.secret:helpdesk_secret_key_32_bytes_min_length_for_hmac_sha}")
    private String secretKey;

    @Value("${api.security.token.expiration:86400000}") //86400000 = 24 horas em milisegundos
    private long expirationTime;

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(Usuario usuario){
        return Jwts.builder()
        .subject(usuario.getEmail())
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
        } catch (Exception e){
            return null; //Token inválido ou inspirado
        }
        
    }
}
