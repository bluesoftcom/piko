package com.bluesoft.piko.admin;

import com.bluesoft.piko.admin.auth.HmacJwtTokenVerifier;
import com.bluesoft.piko.admin.auth.JwtTokenVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PikoTestsConfiguration {

    @Bean
    public JwtTokenVerifier jwtTokenVerifier() {
        return new HmacJwtTokenVerifier();
    }

}
