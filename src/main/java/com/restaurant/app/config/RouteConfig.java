package com.restaurant.app.config;

import com.restaurant.app.auth.AuthController;
import com.restaurant.app.auth.AuthInterceptor;
import com.restaurant.app.user.PermissionInterceptor;
import com.restaurant.app.user.UserRepository;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class RouteConfig implements WebMvcConfigurer {
    private final AuthController authController;
    private final UserRepository userRepository;

    public RouteConfig(AuthController authController, UserRepository userRepository) {
        this.authController = authController;
        this.userRepository = userRepository;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        AuthInterceptor authReq = new AuthInterceptor(false, authController);
        AuthInterceptor authOpt = new AuthInterceptor(true, authController);
        PermissionInterceptor chefReq = new PermissionInterceptor("chef", authController, userRepository);
        PermissionInterceptor adminReq = new PermissionInterceptor("admin", authController, userRepository);

        registry.addInterceptor(chefReq).addPathPatterns("/menu/add");
        registry.addInterceptor(chefReq).addPathPatterns("/menu/modify/**");
        registry.addInterceptor(chefReq).addPathPatterns("/menu/delete/**");

        registry.addInterceptor(authReq).addPathPatterns("/order/list");
        registry.addInterceptor(authReq).addPathPatterns("/order/send");
        registry.addInterceptor(chefReq).addPathPatterns("/order/list-all");
        registry.addInterceptor(chefReq).addPathPatterns("/order/modify/**");
        registry.addInterceptor(chefReq).addPathPatterns("/order/delete/**");

        registry.addInterceptor(authReq).addPathPatterns("/reservation/list");
        registry.addInterceptor(authOpt).addPathPatterns("/reservation/send");
        registry.addInterceptor(chefReq).addPathPatterns("/reservation/list-all");
        registry.addInterceptor(chefReq).addPathPatterns("/reservation/validate/**");
        registry.addInterceptor(chefReq).addPathPatterns("/reservation/delete/**");
        registry.addInterceptor(chefReq).addPathPatterns("/reservation/table/list-all");
        registry.addInterceptor(chefReq).addPathPatterns("/reservation/table/add");
        registry.addInterceptor(chefReq).addPathPatterns("/reservation/table/modify/**");
        registry.addInterceptor(chefReq).addPathPatterns("/reservation/table/delete/**");

        registry.addInterceptor(authReq).addPathPatterns("/user/info");
        registry.addInterceptor(adminReq).addPathPatterns("/user/list-all");
        registry.addInterceptor(adminReq).addPathPatterns("/user/modify/**");
        registry.addInterceptor(adminReq).addPathPatterns("/user/delete/**");
    }
}
