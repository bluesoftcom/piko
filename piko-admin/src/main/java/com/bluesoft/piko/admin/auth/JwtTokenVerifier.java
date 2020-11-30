package com.bluesoft.piko.admin.auth;

import com.auth0.jwt.interfaces.DecodedJWT;

public interface JwtTokenVerifier {

    void checkTokenSignature(DecodedJWT token, AuthenticationProperties.AcceptedIssuer issuer) throws UnauthenticatedAccessAttemptException;

}
