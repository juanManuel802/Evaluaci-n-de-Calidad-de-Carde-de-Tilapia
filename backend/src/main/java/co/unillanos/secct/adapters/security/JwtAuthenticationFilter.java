package co.unillanos.secct.adapters.security;

import co.unillanos.secct.infrastructure.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter intercepting incoming requests to authenticate JWT bearer tokens according to DC-HU2 and CB-12.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (isPublicEndpoint(request)) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            writeUnauthorizedResponse(response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (!jwtService.validateToken(token)) {
            writeUnauthorizedResponse(response);
            return;
        }

        Integer userId = jwtService.getUserId(token);
        String roleCode = jwtService.getRoleCode(token);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (roleCode != null && !roleCode.isBlank()) {
            String roleWithPrefix = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
            authorities.add(new SimpleGrantedAuthority(roleWithPrefix));
        }

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, authorities));

        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (request.getContextPath() != null && !request.getContextPath().isEmpty() && path.startsWith(request.getContextPath())) {
            path = path.substring(request.getContextPath().length());
        }

        if ("/actuator/health".equals(path)) {
            return true;
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            return "/api/users".equals(path)
                    || "/api/auth/login".equals(path)
                    || "/api/auth/registro".equals(path);
        }

        return false;
    }

    private void writeUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"Tu sesión no es válida o expiró. Inicia sesión de nuevo.\",\"errors\":[]}");
    }
}
