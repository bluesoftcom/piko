package com.bluesoft.piko.locations.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.time.Instant;
import java.util.Date;

public class LoggedUserFixtures {

    public static LoggedUser.LoggedUserBuilder someLoggedUser() {
        return someLoggedUser("trevornooah");
    }

    public static LoggedUser.LoggedUserBuilder someLoggedUser(String username) {
        return LoggedUser.builder()
                .username(username)
                .email(String.format("%s@email.test", username))
                .idToken(
                        JWT.create()
                                .withIssuer("test-issuer")
                                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                                .withClaim("cognito:username", username)
                                .withClaim("email", String.format("%s@email.test", username))
                                .sign(Algorithm.HMAC256(HmacJwtTokenVerifier.SIGNING_SECRET))
                );
    }

}