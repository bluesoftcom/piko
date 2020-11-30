package com.bluesoft.piko.admin.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import java.time.Instant;
import java.util.List;

import static com.bluesoft.piko.admin.auth.AuthenticationProperties.AcceptedIssuer;

@Component
@AllArgsConstructor
public class LoggedUserInterceptor implements WebRequestInterceptor {

    private final AuthenticationProperties authenticationProperties;
    private final JwtTokenVerifier jwtTokenVerifier;

    @Override
    public void preHandle(WebRequest request) {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null) {
            throw new UnauthenticatedAccessAttemptException("missing-authorization-header");
        }

        try {
            final DecodedJWT decodedToken = JWT.decode(authorization);
            final AcceptedIssuer issuer = findAcceptedIssuer(
                    authenticationProperties.getAcceptedIssuers(),
                    decodedToken.getIssuer()
            );
            jwtTokenVerifier.checkTokenSignature(decodedToken, issuer);

            if (decodedToken.getExpiresAt().toInstant().isBefore(Instant.now())) {
                throw new UnauthenticatedAccessAttemptException("token-expired");
            }
        } catch (JWTDecodeException e) {
            throw new UnauthenticatedAccessAttemptException("token-decoding-error", e);
        }
    }

    @Override
    public void postHandle(WebRequest request, ModelMap model) {
    }

    @Override
    public void afterCompletion(WebRequest request, Exception ex) {
    }

    private static AcceptedIssuer findAcceptedIssuer(List<AcceptedIssuer> issuers, String issuer) {
        return issuers.stream()
                .filter(acceptedIssuer -> acceptedIssuer.getUrl().equals(issuer))
                .findFirst()
                .orElseThrow(() -> new UnauthenticatedAccessAttemptException("unknown-token-issuer"));
    }


}
