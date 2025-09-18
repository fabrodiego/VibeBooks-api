package com.vibebooks.api.security;

import com.vibebooks.api.repository.UsuarioRepository;
import com.vibebooks.api.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
public class SecurityFilter extends OncePerRequestFilter{

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("\n=====================================");
        System.out.println("FILTRO ACIONADO PARA A ROTA: " + request.getRequestURI());

        String tokenJWT = recuperarToken(request);

        if (tokenJWT != null) {
            System.out.println("[ETAPA 1 SUCESSO] Token recuperado do cabeçalho: " + tokenJWT.substring(0, 15) + "..."); // Mostra só o início
            try {
                String subject = tokenService.getSubject(tokenJWT);
                System.out.println("[ETAPA 2 SUCESSO] Subject (ID do usuário) extraído: " + subject);

                UUID usuarioId = UUID.fromString(subject);
                System.out.println("[ETAPA 3 SUCESSO] String do Subject convertida para UUID.");

                UserDetails usuario = usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new RuntimeException("Usuário do token não encontrado no banco de dados"));

                System.out.println("[ETAPA 4 SUCESSO] Usuário encontrado no banco: " + usuario.getUsername());

                var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("[ETAPA 5 SUCESSO] Usuário autenticado e definido no contexto de segurança.");

            } catch (Exception e) {
                System.err.println("[FALHA EM UMA DAS ETAPAS] ERRO: " + e.getClass().getName() + " - " + e.getMessage());
            }
        } else {
            System.err.println("[FALHA NA ETAPA 1] Nenhum token JWT encontrado no cabeçalho Authorization após verificação robusta.");
        }

        filterChain.doFilter(request, response);
        System.out.println("Filtro finalizado.");
        System.out.println("=====================================");
    }

    private String recuperarToken(HttpServletRequest request) {
        var headersNames = request.getHeaderNames();
        if (headersNames != null) {
            while (headersNames.hasMoreElements()) {
                String headerName = headersNames.nextElement();
                if (headerName.equalsIgnoreCase("Authorization")) {
                    String authorizationHeader = request.getHeader(headerName);
                    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                        return authorizationHeader.substring(7);
                    }
                }
            }
        }
        return null;
    }
}
