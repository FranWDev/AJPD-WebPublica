package org.dubini.frontend_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    private boolean isBlockedUrl(String path) {
        return path.equals("/api/cache/activities/clear") ||
                path.equals("/api/cache/news/clear") ||
                path.equals("/api/service-workers/update");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (isBlockedUrl(path)) {
            String token = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("jwt".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            if (token == null) {
                log.warn("[AUTH] Acceso denegado a '{}' desde IP '{}': Cookie 'jwt' no encontrada",
                        path, request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("No JWT token found");
                return;
            }

            boolean isValid = jwtProvider.validateToken(token);

            if (!isValid) {
                log.warn("[AUTH] Acceso denegado a '{}' desde IP '{}': Token JWT inválido o expirado",
                        path, request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid JWT token");
                return;
            }

            log.info("[AUTH] Petición autenticada con éxito para ruta protegida '{}'", path);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("backoffice", null,
                    Collections.emptyList());
            SecurityContextHolder.clearContext();
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);

    }
}