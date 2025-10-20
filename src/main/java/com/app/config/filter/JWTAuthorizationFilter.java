package com.app.config.filter;

import com.app.config.jwt.JwtUtils;
import com.app.config.security.EmailAuthenticationToken;
import com.app.exception.auth.UnauthorizedException;
import com.app.service.UserDetailServiceImp;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthorizationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserDetailServiceImp userDetailServiceImp;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (!jwtUtils.validateToken(token)) {
                throw new UnauthorizedException();
            }

            String email = jwtUtils.getEmailFromToken(token);
            if (email == null) {
                throw new UnauthorizedException();
            }
            UserDetails userDetails = userDetailServiceImp.loadUserByEmail(email);
            EmailAuthenticationToken auth = new EmailAuthenticationToken(email, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }
}
