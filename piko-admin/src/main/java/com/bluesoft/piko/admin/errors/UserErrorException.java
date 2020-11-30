package com.bluesoft.piko.admin.errors;

import lombok.Getter;

import java.util.Map;

@Getter
public class UserErrorException extends RuntimeException {

    private final String code;
    private final Map<String, String> details;

    public UserErrorException(String code, Map<String, String> details) {
        super("There was an user-error with code: " + code + " and details: " + details);
        this.code = code;
        this.details = details;
    }

    public UserErrorException(String code, Map<String, String> details, Throwable cause) {
        super("There was an user-error with code: " + code + " and details: " + details, cause);
        this.code = code;
        this.details = details;
    }

    public UserErrorException(String code) {
        this(code, Map.of());
    }

    public UserErrorException(String code, Throwable cause) {
        this(code, Map.of(), cause);
    }

}
