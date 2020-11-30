package com.bluesoft.piko.locations.auth;

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
