package com.restaurant.app.auth;

import jakarta.servlet.http.*;
import org.springframework.http.*;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;
import java.util.Optional;

public class AuthInterceptor implements HandlerInterceptor {
    private final boolean optional;
    private final AuthController authController;

    public AuthInterceptor(boolean optional, AuthController authController) {
        this.optional = optional;
        this.authController = authController;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) {
            if (optional) {
                return true;
            }

            response.setStatus(401);
            return false;
        }

        String[] tok = header.split(" ", 2);
        if (tok.length != 2 || !Objects.equals(tok[0], "Bearer")) {
            response.setStatus(401);
            return false;
        }
        request.setAttribute("sessionToken", tok[1]);

        Optional<String> userEmail = authController.isSessionAlive(tok[1]);
        if (userEmail.isEmpty()) {
            response.setStatus(401);
            return false;
        }
        request.setAttribute("authenticated", true);
        request.setAttribute("userEmail", userEmail.get());

        return true;
    }
}
