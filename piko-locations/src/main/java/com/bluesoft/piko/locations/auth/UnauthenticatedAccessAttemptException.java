package com.bluesoft.piko.locations.auth;

import lombok.Getter;

@Getter
public class UnauthenticatedAccessAttemptException extends RuntimeException {

    private final String code;

    public UnauthenticatedAccessAttemptException(String code) {
        super("Detected unauthenticated access attempt with failure cause: " + code);
        this.code = code;
    }

    public UnauthenticatedAccessAttemptException(String code, Throwable e) {
        super("Detected unauthenticated access attempt with failure cause: " + code, e);
        this.code = code;
    }
}
