package com.bluesoft.piko.admin.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public class LoggedUserAuthentication {
    public static RequestPostProcessor authenticatedUser(LoggedUser loggedUser) {
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, loggedUser.getIdToken());
            return request;
        };
    }
}
