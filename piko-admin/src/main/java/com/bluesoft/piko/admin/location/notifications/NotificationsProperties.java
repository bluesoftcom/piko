package com.bluesoft.piko.admin.location.notifications;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ConfigurationProperties("notifications")
@Component
@Data
@Validated
public class NotificationsProperties {

    @NotBlank
    private String fromAddress;

    @NotNull
    @Valid
    private Notification locationPublished = new Notification();

    @Data
    public static class Notification {

        @NotBlank
        private String subjectTemplate;
        @NotNull
        private Resource bodyTemplate;

    }
}
