package ua.yarynych.apiaccountmanagement.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ua.yarynych.apiaccountmanagement.entity.exceptions.BadTokenException;
import ua.yarynych.apiaccountmanagement.entity.auth.UserAuthDetails;
import ua.yarynych.apiaccountmanagement.service.auth.JwtService;

import java.io.IOException;

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    @Value("${app.refresh.link}")
    private String REFRESH_LINK;

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        if(request.getRequestURI().contains("/health/check")) {
            chain.doFilter(request, response);
        }

        String token = authHeader.substring(7);

        try {
            handleToken(request, response, chain, token);
        } catch (BadTokenException ex) {
            log.warn("Invalid token: {}", ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }


    private void handleToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String token) throws IOException, ServletException {

        jwtService.validateToken(token);

        switch (jwtService.extractType(token)) {
            case "access" -> {
                UserAuthDetails userDetails = jwtService.read(token);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                log.info("Final authentication authorities: {}", authenticationToken.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) {
                    log.info("Current user: {}", auth.getName());
                    log.info("User authorities: {}", auth.getAuthorities());
                }

                chain.doFilter(request, response);
            }
            case "refresh" -> {
                if (REFRESH_LINK.equals(new ServletServerHttpRequest(request).getURI().getRawPath())) {
                    chain.doFilter(request, response);
                } else {
                    log.warn("<Refresh Token> use for refresh!");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "<Refresh Token> use for refresh!");
                }
            }
            default -> {
                log.warn("Unknown token type: {}", token);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unknown token type!");
            }
        }
    }
}
