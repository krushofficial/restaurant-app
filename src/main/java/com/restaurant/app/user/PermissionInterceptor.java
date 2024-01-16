package com.restaurant.app.user;

import com.restaurant.app.auth.AuthController;
import com.restaurant.app.auth.AuthInterceptor;
import jakarta.servlet.http.*;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

public class PermissionInterceptor implements HandlerInterceptor {
    private final String reqLevel;
    private final AuthInterceptor authInterceptor;
    private final UserRepository userRepository;

    public PermissionInterceptor(String reqLevel, AuthController authController, UserRepository userRepository) {
        this.reqLevel = reqLevel;
        this.authInterceptor = new AuthInterceptor(false, authController);
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!authInterceptor.preHandle(request, response, handler)) {
            return false;
        }

        Optional<User> _user = userRepository.findById((String)request.getAttribute("userEmail"));
        if (_user.isEmpty()) {
            response.setStatus(500);
            return false;
        }
        User user = _user.get();

        if (!user.hasPermission(reqLevel)) {
            response.setStatus(403);
            return false;
        }

        return true;
    }
}
