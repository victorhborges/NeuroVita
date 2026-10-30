package com.neurovita.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaRoutingConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        // Rotas do Angular, excluindo API, documentação e arquivos estáticos.
        String rotaFrontend =
                "/{path:^(?!api$|swagger-ui$|v3$|img$)[^\\.]+}";

        registry.addViewController(rotaFrontend)
                .setViewName("forward:/index.html");

        registry.addViewController(rotaFrontend + "/**")
                .setViewName("forward:/index.html");
    }
}
