package com.bluesoft.piko.locations.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Data
@Validated
@ConfigurationProperties("auth")
@Component
public class AuthenticationProperties {

    @NotBlank
    private String usernameClaim;

    @NotBlank
    private String emailClaim;

    @NotNull
    @NotEmpty
    private List<AcceptedIssuer> acceptedIssuers = new ArrayList<>();

    @Data
    public static class AcceptedIssuer {

        @NotBlank
        private String name;

        @NotBlank
        private String url;

        @NotNull
        private URL jwksUrl;

    }
}
