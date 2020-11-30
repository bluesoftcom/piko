package com.bluesoft.piko.locations.auth;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;

import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static com.bluesoft.piko.locations.auth.AuthenticationProperties.AcceptedIssuer;

public class JwksJwtTokenVerifier implements JwtTokenVerifier {

    @Override
    public void checkTokenSignature(DecodedJWT token, AcceptedIssuer issuer) throws UnauthenticatedAccessAttemptException {
        final UrlJwkProvider provider = new UrlJwkProvider(issuer.getJwksUrl());
        try {
            final RSAKeyProvider keyProvider = jwksKeyProvider(token, provider);
            Algorithm.RSA256(keyProvider).verify(token);
        } catch (JwkException e) {
            throw new UnauthenticatedAccessAttemptException("jwks-fetching-error", e);
        } catch (SignatureVerificationException e) {
            throw new UnauthenticatedAccessAttemptException("signature-verification-error", e);
        }
    }

    private static RSAKeyProvider jwksKeyProvider(DecodedJWT decodedToken, UrlJwkProvider provider) throws JwkException {
        final PublicKey publicKey = provider.get(decodedToken.getKeyId()).getPublicKey();

        return new RSAKeyProvider() {
            @Override
            public RSAPublicKey getPublicKeyById(String keyId) {
                return (RSAPublicKey) publicKey;
            }

            @Override
            public RSAPrivateKey getPrivateKey() {
                return null;
            }

            @Override
            public String getPrivateKeyId() {
                return null;
            }
        };
    }

}
