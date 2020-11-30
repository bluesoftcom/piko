package com.bluesoft.piko.locations;

import com.bluesoft.piko.locations.auth.JwksJwtTokenVerifier;
import com.bluesoft.piko.locations.auth.JwtTokenVerifier;
import com.bluesoft.piko.locations.auth.LoggedUserArgumentResolver;
import com.bluesoft.piko.locations.auth.LoggedUserInterceptor;
import com.bluesoft.piko.locations.location.messaging.LocationEventsBindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableBinding(LocationEventsBindings.class)
public class PikoLocationsConfiguration {

    @Bean
    @Profile("!test")
    public JwtTokenVerifier jwtTokenVerifier() {
        return new JwksJwtTokenVerifier();
    }

    @Bean
    public WebMvcConfigurer authenticationConfigurer(LoggedUserArgumentResolver loggedUserArgumentResolver,
                                                     LoggedUserInterceptor loggedUserInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addWebRequestInterceptor(loggedUserInterceptor);
            }

            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(loggedUserArgumentResolver);
            }
        };
    }

}
