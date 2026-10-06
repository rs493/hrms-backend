package com.hrms.backend.identity.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authorizationHeader)
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authorizationHeader
                        .substring(BEARER_PREFIX.length())
                        .trim();

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.parseAndValidateToken(token);

            String userId = claims.getSubject();

            String email = claims.get("email", String.class);

            List<String> roles =
                    claims.get("roles", List.class);

            List<String> permissions =
                    claims.get("permissions", List.class);

            if (StringUtils.hasText(userId)) {

                List<SimpleGrantedAuthority> authorities =
                        new ArrayList<>();

                if (roles != null) {
                    roles.forEach(role ->
                            authorities.add(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role)));
                }

                if (permissions != null) {
                    permissions.forEach(permission ->
                            authorities.add(
                                    new SimpleGrantedAuthority(
                                            "PERM_" + permission)));
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                authorities);

                authentication.setDetails(email);

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

            } else {
                SecurityContextHolder.clearContext();
            }

        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            log.warn("JWT authentication failed");
        }

        filterChain.doFilter(request, response);
    }
}