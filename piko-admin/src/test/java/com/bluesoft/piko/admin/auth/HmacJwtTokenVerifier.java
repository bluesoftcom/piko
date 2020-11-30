package com.bluesoft.piko.admin.auth;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.UUID;

import static com.bluesoft.piko.admin.auth.AuthenticationProperties.AcceptedIssuer;

public class HmacJwtTokenVerifier implements JwtTokenVerifier {

    public static String SIGNING_SECRET = UUID.randomUUID().toString();

    @Override
    public void checkTokenSignature(DecodedJWT token, AcceptedIssuer issuer) throws UnauthenticatedAccessAttemptException {
        try {
            Algorithm.HMAC256(SIGNING_SECRET).verify(token);
        } catch (SignatureVerificationException e) {
            throw new UnauthenticatedAccessAttemptException("signature-verification-error", e);
        }
    }
}
