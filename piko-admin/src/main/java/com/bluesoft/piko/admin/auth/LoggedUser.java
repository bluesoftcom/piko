package com.bluesoft.piko.admin.auth;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Value
@Builder
public class LoggedUser {

    String username;
    String email;

    @ToString.Exclude
    String idToken;

}
