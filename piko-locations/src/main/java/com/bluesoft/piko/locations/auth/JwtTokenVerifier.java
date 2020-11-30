package com.bluesoft.piko.locations.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.bluesoft.piko.locations.auth.AuthenticationProperties.AcceptedIssuer;

public interface JwtTokenVerifier {

    void checkTokenSignature(DecodedJWT token, AcceptedIssuer issuer) throws UnauthenticatedAccessAttemptException;

}
