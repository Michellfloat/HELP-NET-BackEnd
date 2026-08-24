package com.example.helpdesk_backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.helpdesk_backend.model.Usuario;
import com.example.helpdesk_backend.model.enums.Perfil;
import com.example.helpdesk_backend.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner{
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByEmail("admin@helpdesk.com").isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador do Sistema");
            admin.setEmail("admin@helpdesk.com");
            admin.setSenha(passwordEncoder.encode("123"));
            admin.setPerfil(Perfil.ADMIN);
            admin.setCargo("Administrador Geral");
            admin.setSetor("Tecnologia da Informação");
            admin.setCadastroCompleto(true);

            usuarioRepository.save(admin);
            System.out.println(">>> USUÁRIO ADMIN CRIADO COM SUCESSO: admin@helpdesk.com / 123 (Perfil: ADMIN) <<<");
        }
    }
}
