package com.vibebooks.api.service;

import com.vibebooks.api.model.Usuario;
import com.vibebooks.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cria um novo usuário no sistema, aplicando as regras de negócio.
     * @param nomeUsuario O nome de usuário escolhido.
     * @param email O e-mail do novo usuário.
     * @param senhaPura A senha em texto plano (não criptografada).
     * @return O objeto Usuario que foi salvo no banco de dados.
     * @throws IllegalStateException se o nome de usuário ou e-mail já existirem.
     */
    @Transactional
    public Usuario criarUsuario(String nomeUsuario, String email, String senhaPura) {

        // --- REGRA DE NEGÓCIO 1: VERIFICAR DUPLICIDADE ---
        if (usuarioRepository.findByNomeUsuario(nomeUsuario).isPresent()) {
            throw new IllegalStateException("Nome de usuário ou e-mail já cadastrado.");
        }
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("O e-mail '" + email + "' já está cadastrado.");
        }

        // --- REGRA DE NEGÓCIO 2: CRIPTOGRAFAR A SENHA ---
        String senhaCriptografada = passwordEncoder.encode(senhaPura);

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNomeUsuario(nomeUsuario);
        novoUsuario.setEmail(email);
        novoUsuario.setSenhaHash(senhaCriptografada);

        return usuarioRepository.save(novoUsuario);
    }
}
