package com.bluesoft.piko.admin;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.bluesoft.piko.admin.auth.*;
import com.bluesoft.piko.admin.location.messaging.LocationEventsBindings;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableBinding(LocationEventsBindings.class)
public class PikoAdminConfiguration {

    @Bean
    @Profile("!test")
    public JwtTokenVerifier jwtTokenVerifier() {
        return new JwksJwtTokenVerifier();
    }

    @Bean
    public AWSCognitoIdentityProvider awsCognitoIdentityProvider(CognitoProperties cognitoProperties) {
        return AWSCognitoIdentityProviderClient.builder()
                .withEndpointConfiguration(
                        cognitoProperties.getEndpoint() != null
                                ? new AwsClientBuilder.EndpointConfiguration(cognitoProperties.getEndpoint().toExternalForm(), "eu-west-1")
                                : null
                )
                .build();
    }

    @Configuration
    @Profile("!test")
    @EnableAsync
    public static class AsyncConfiguration {
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
