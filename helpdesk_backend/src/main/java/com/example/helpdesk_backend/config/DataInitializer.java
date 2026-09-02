package com.example.helpdesk_backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.model.enums.NivelAtendente;
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.model.enums.Setor;
import com.example.helpdesk_backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner{
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        criarAdminSeNaoExistir();
        criarUsuarioComumSeNaoExistir();
        criarAtendenteNivelIIISeNaoExistir();
        criarAtendenteNivelISeNaoExistir();
    }

    private void criarAdminSeNaoExistir(){
        if (usuarioRepository.findByEmail("admin@helpdesk.com").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador do Sistema");
            admin.setEmail("admin@helpdesk.com");
            admin.setSenha(passwordEncoder.encode("Str0ngP4ssw0rd!"));
            admin.setPerfil(Perfil.ADMIN);
            admin.setCargo("Administrador Geral");
            admin.setSetor(Setor.DESENVOLVIMENTO);

            usuarioRepository.save(admin);
            System.out.println(">>> USUÁRIO ADMIN CRIADO: admin@helpdesk.com / Str0ngP4ssw0rd! <<<");
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
            System.out.println(">>> USUÁRIO COMUM CRIADO: eri.matsunaga@helpdesk.com / Senh4F0rte456! <<<");
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
