package com.bluesoft.piko.locations;

import com.bluesoft.piko.locations.auth.HmacJwtTokenVerifier;
import com.bluesoft.piko.locations.auth.JwtTokenVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PikoTestsConfiguration {

    @Bean
    public JwtTokenVerifier jwtTokenVerifier() {
        return new HmacJwtTokenVerifier();
    }

}
