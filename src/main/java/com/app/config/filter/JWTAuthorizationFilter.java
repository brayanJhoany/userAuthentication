package com.app.config.filter;

import com.app.config.jwt.JwtUtils;
import com.app.service.UserDetailServiceImp;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    UserDetailServiceImp userDetailServiceImp;


    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request ,
                                    @NotNull  HttpServletResponse response ,
                                    @NotNull  FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer")) {
            token = token.substring(7);
            if(jwtUtils.validateToken(token)){
                String username = jwtUtils.getUsernameFromToken(token);
                UserDetails userDetails = userDetailServiceImp.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        username,null, userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                filterChain.doFilter(request, response);
                
            }else{
                handleAuthError(response, request, "Token is not valid");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
    private void handleAuthError(HttpServletResponse response, HttpServletRequest request, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, Object> error = new HashMap<>();
        error.put("message", message);
        error.put("status", 401);
        error.put("error", "Unauthorized");
        error.put("timestamp", Instant.now().toString());
        error.put("path", request.getRequestURI());

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), error);
    }

}
