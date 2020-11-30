package com.bluesoft.piko.locations.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoggedUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthenticationProperties authenticationProperties;

    public LoggedUserArgumentResolver(AuthenticationProperties authenticationProperties) {
        this.authenticationProperties = authenticationProperties;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterType().equals(LoggedUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter,
                                  @Nullable ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest,
                                  @Nullable WebDataBinderFactory webDataBinderFactory) throws Exception {
        final String authorization = nativeWebRequest.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null) {
            throw new MissingRequestHeaderException(HttpHeaders.AUTHORIZATION, methodParameter);
        }

        try {
            final DecodedJWT decodedToken = JWT.decode(authorization);
            return LoggedUser.builder()
                    .username(
                            findClaimValue(decodedToken, authenticationProperties.getUsernameClaim())
                    )
                    .email(
                            findClaimValue(decodedToken, authenticationProperties.getEmailClaim())
                    )
                    .idToken(authorization)
                    .build();
        } catch (JWTDecodeException e) {
            throw new UnauthenticatedAccessAttemptException("token-decoding-error", e);
        }
    }

    private static String findClaimValue(DecodedJWT decodedToken, String claim) {
        return decodedToken.getClaim(claim).asString();
    }

}
