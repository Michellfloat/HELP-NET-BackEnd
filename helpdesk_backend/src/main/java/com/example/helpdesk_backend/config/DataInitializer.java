package com.example.helpdesk_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.model.enums.NivelAtendente;
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.model.enums.Setor;
import com.example.helpdesk_backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Semeia as contas iniciais.
 *
 * Duas coisas separadas moram aqui, e so agora com nomes diferentes:
 *
 * - O ADMIN e o bootstrap do sistema. Precisa existir em TODO ambiente, senao nao ha
 *   por onde entrar depois do deploy. Antes vinha com senha fixa no codigo; agora
 *   e-mail e senha saem de `app.admin.*`, entao producao usa ADMIN_SENHA e o
 *   repositorio deixa de carregar a credencial de um ambiente publicado.
 *
 * - Os tres usuarios de demonstracao (solicitante, atendente III e atendente I) sao
 *   dados de teste, com senha conhecida e escrita aqui. Ficam atras de
 *   `app.seed.demo`, desligado em producao.
 *
 * A separacao existe porque desligar o arquivo inteiro em producao -- que era a
 * alternativa -- resolveria as senhas de demonstracao criando um problema pior: um
 * ambiente publicado sem nenhum usuario, impossivel de acessar.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner{
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@helpdesk.com}")
    private String adminEmail;

    @Value("${app.admin.senha:Str0ngP4ssw0rd!}")
    private String adminSenha;

    /** Desligado em producao pelo application-prod.properties. */
    @Value("${app.seed.demo:true}")
    private boolean semearDemonstracao;

    @Override
    public void run(String... args) {
        criarAdminSeNaoExistir();

        if (!semearDemonstracao) {
            return;
        }

        criarUsuarioComumSeNaoExistir();
        criarAtendenteNivelIIISeNaoExistir();
        criarAtendenteNivelISeNaoExistir();
    }

    private void criarAdminSeNaoExistir(){
        if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador do Sistema");
            admin.setEmail(adminEmail);
            admin.setSenha(passwordEncoder.encode(adminSenha));
            admin.setPerfil(Perfil.ADMIN);
            admin.setCargo("Administrador Geral");
            admin.setSetor(Setor.DESENVOLVIMENTO);

            usuarioRepository.save(admin);
            // Sem a senha no log: em producao ela vem de variavel de ambiente e nao
            // deve acabar no stdout do servidor.
            System.out.println(">>> USUÁRIO ADMIN CRIADO: " + adminEmail + " (Perfil: ADMIN) <<<");
        }
    }

    private void criarUsuarioComumSeNaoExistir(){
        if (usuarioRepository.findByEmail("eri.matsunaga@helpdesk.com").isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setNome("Eri Matsunaga");
            usuario.setEmail("eri.matsunaga@helpdesk.com");
            usuario.setSenha(passwordEncoder.encode("SenhaForte123!"));
            usuario.setPerfil(Perfil.USUARIO);
            usuario.setCargo("Coordenadora Administrativa");
            usuario.setSetor(Setor.ADMINISTRATIVO);

            usuarioRepository.save(usuario);
            System.out.println(">>> USUÁRIO COMUM CRIADO: eri.matsunaga@helpdesk.com / SenhaForte123! <<<");
        }
    }

    private void criarAtendenteNivelIIISeNaoExistir(){
        if(usuarioRepository.findByEmail("klein.moretti@helpdesk.com").isEmpty()){
            Usuario atendente = new Usuario();
            atendente.setNome("Klein Moretti");
            atendente.setEmail("klein.moretti@helpdesk.com");
            atendente.setSenha(passwordEncoder.encode("Klein123!@#"));
            atendente.setPerfil(Perfil.ATENDENTE);
            atendente.setNivelAtendente(NivelAtendente.NIVEL_III);
            atendente.setCargo("Desenvolvedor Sênior de Desenvolvimento");
            atendente.setSetor(Setor.DESENVOLVIMENTO);

            usuarioRepository.save(atendente);
            System.out.println(">>> ATENDENTE NIVEL III CRIADO: klein.moretti@helpdesk.com / Klein123!@# <<<");
        }
    }

    private void criarAtendenteNivelISeNaoExistir() {
        if (usuarioRepository.findByEmail("tive.korendu@helpdesk.com").isEmpty()) {
            Usuario atendente = new Usuario();
            atendente.setNome("Tive Korendu");
            atendente.setEmail("tive.korendu@helpdesk.com");
            atendente.setSenha(passwordEncoder.encode("Media123"));
            atendente.setPerfil(Perfil.ATENDENTE);
            atendente.setNivelAtendente(NivelAtendente.NIVEL_I);
            atendente.setCargo("Auxiliar técnico de Desenvolvimento");
            atendente.setSetor(Setor.DESENVOLVIMENTO);

            usuarioRepository.save(atendente);
            System.out.println(">>> ATENDENTE NIVEL I CRIADO: tive.korendu@helpdesk.com / Media123 <<<");
        }
    }
}
