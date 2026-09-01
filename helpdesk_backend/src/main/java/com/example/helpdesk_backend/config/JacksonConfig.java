package com.example.helpdesk_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class JacksonConfig {

    /**
     * ObjectMapper (Jackson 2) usado pelo SecurityConfig para escrever o StandardError
     * das respostas 401 e 403.
     *
     * Duas configuracoes sao obrigatorias aqui, e nao sao opcionais:
     *
     * 1) findAndRegisterModules() -- StandardError.timestamp e um LocalDateTime, e o
     *    Jackson 2 nao serializa java.time sem o modulo JSR-310. Sem esta linha o mapper
     *    escrevia `{"timestamp"` e entao lancava InvalidDefinitionException: a resposta ja
     *    tinha sido comitada com status 401, entao o cliente recebia um JSON truncado de
     *    12 bytes em TODA requisicao sem token ou sem permissao. O jackson-datatype-jsr310
     *    ja vem no classpath; so faltava registra-lo.
     *
     * 2) WRITE_DATES_AS_TIMESTAMPS desabilitado -- com o modulo registrado mas esta flag
     *    no padrao, o LocalDateTime sairia como array ([2026,9,1,12,29,51]), diferente do
     *    formato ISO-8601 que o GlobalExceptionHandler produz (esse passa pelo Jackson 3
     *    do Spring MVC). Os dois caminhos de erro precisam do mesmo formato, senao o
     *    cliente tem que saber de qual deles a resposta veio.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
