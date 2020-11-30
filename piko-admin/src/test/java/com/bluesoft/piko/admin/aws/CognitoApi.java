package com.bluesoft.piko.admin.aws;

import com.bluesoft.piko.admin.auth.CognitoProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Component
@Slf4j
public class CognitoApi implements Closeable {

    private final WireMockServer server;
    private final CognitoProperties cognitoProperties;

    public CognitoApi(CognitoProperties cognitoProperties) {
        Objects.requireNonNull(cognitoProperties.getEndpoint(), "For testing purposes Cognito endpoint is required");
        final int port = cognitoProperties.getEndpoint().getPort();
        WireMockServer server = new WireMockServer(
                WireMockConfiguration.wireMockConfig().port(port)
        );
        log.info("Starting Cognito API mock server at port: {}", port);
        server.start();
        this.server = server;
        this.cognitoProperties = cognitoProperties;
    }

    @SneakyThrows
    public void mockSuccessfulAdminGetUser(String username) {
        final String json = "{\n" +
                "  \"Enabled\": true,\n" +
                "  \"UserAttributes\": [\n" +
                "    {\n" +
                "      \"Name\": \"sub\",\n" +
                "      \"Value\": \"00a7b5ef-9129-47b1-b1ab-8e2ead5a36a4\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"Name\": \"email_verified\",\n" +
                "      \"Value\": \"true\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"Name\": \"email\",\n" +
                "      \"Value\": \"" + String.format("%s@email.test", username) + "\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"UserCreateDate\": 1.586685879954E9,\n" +
                "  \"UserLastModifiedDate\": 1.586686519585E9,\n" +
                "  \"UserStatus\": \"CONFIRMED\",\n" +
                "  \"Username\": \"" + username + "\"\n" +
                "}";
        server.stubFor(
                post(urlPathEqualTo(cognitoProperties.getEndpoint().getPath()))
                        .andMatching(request ->
                                request.header("X-Amz-Target").firstValue()
                                        .equals("AWSCognitoIdentityProviderService.AdminGetUser")
                                        ? MatchResult.exactMatch()
                                        : MatchResult.noMatch()
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody(json)
                        )
        );
    }

    public void reset() {
        log.info("Resetting Cognito API mocks");
        server.resetAll();
    }

    @Override
    public void close() {
        log.info("Gracefully shutting down Cognito API mock server");
        server.stop();
        log.info("Cognito API mock server shut down");
    }
}
