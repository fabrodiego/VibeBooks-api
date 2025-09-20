package com.vibebooks.api.security;

import com.vibebooks.api.repository.UsuarioRepository;
import com.vibebooks.api.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);
    private static final String AUTH_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug("Filtro de segurança acionado para a rota: {}", request.getRequestURI());

        String tokenJWT = recuperarToken(request);

        if (tokenJWT != null) {
            try {
                String subject = tokenService.getSubject(tokenJWT);
                logger.debug("Subject (ID do usuário) extraído do token: {}", subject);

                UUID usuarioId = UUID.fromString(subject);

                UserDetails usuario = usuarioRepository.findById(usuarioId)
                        .orElseThrow(() -> new RuntimeException("Usuário do token não encontrado no banco de dados"));

                logger.info("Usuário '{}' encontrado. Autenticando...", usuario.getUsername());

                var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("Usuário '{}' autenticado com sucesso e definido no Contexto de Segurança.", usuario.getUsername());

            } catch (Exception e) {
                // É uma boa prática limpar o contexto de segurança em caso de erro na validação do token.
                SecurityContextHolder.clearContext();
                logger.error("Falha na validação do token JWT: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            }
        }
        // Se o tokenJWT for nulo, não fazemos nada. A requisição continua na cadeia de filtros.
        // Se o endpoint for protegido, o Spring Security irá bloqueá-lo.
        // Se for um endpoint público, será permitido. Isso evita o log desnecessário.

        filterChain.doFilter(request, response);
        logger.debug("Filtro de segurança finalizado para a rota: {}", request.getRequestURI());
    }

    private String recuperarToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTH_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
