package com.bluesoft.piko.admin.auth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.net.URL;

@ConfigurationProperties("cognito")
@Data
@Validated
@Component
public class CognitoProperties {

    @Nullable
    private URL endpoint;

    @NotBlank
    private String userPoolId;

}
