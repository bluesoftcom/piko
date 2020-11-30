package com.bluesoft.piko.admin.location.notifications;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.bluesoft.piko.admin.auth.CognitoProperties;
import com.bluesoft.piko.admin.location.json.DetailedJsonLocation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.InputStream;

import static com.bluesoft.piko.admin.location.notifications.NotificationsProperties.Notification;

@AllArgsConstructor
@Component
@Slf4j
public class AdminLocationsNotifications {

    public static final String EMAIL_ATTRIBUTE = "email";

    private final CognitoProperties cognitoProperties;
    private final NotificationsProperties notificationsProperties;
    private final AWSCognitoIdentityProvider cognito;
    private final JavaMailSender mailSender;

    @SneakyThrows
    @Async
    public void sendLocationPublishedEmail(DetailedJsonLocation location) {
        log.info("Sending notification to: {} about published location: {}", location.getOwner(), location.getId());
        final AdminGetUserResult result = cognito.adminGetUser(
                new AdminGetUserRequest()
                        .withUserPoolId(cognitoProperties.getUserPoolId())
                        .withUsername(location.getOwner())
        );

        final AttributeType email = findAttribute(result, EMAIL_ATTRIBUTE);
        final Notification templates = notificationsProperties.getLocationPublished();

        final String subjectTemplate = templates.getSubjectTemplate();
        final String bodyTemplate = readResource(templates.getBodyTemplate());

        final String subject = processTemplate(subjectTemplate, location);
        final String body = processTemplate(bodyTemplate, location);

        final MimeMessageHelper message = new MimeMessageHelper(mailSender.createMimeMessage());
        message.addTo(email.getValue());
        message.setFrom(notificationsProperties.getFromAddress());
        message.setSubject(subject);
        message.setText(body, true);
        mailSender.send(message.getMimeMessage());
    }


    @SneakyThrows
    private static String readResource(Resource resource) {
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes());
        }
    }

    private static String processTemplate(String subjectTemplate, DetailedJsonLocation location) {
        return subjectTemplate
                .replace("${username}", location.getOwner())
                .replace("${location_name}", location.getLocation().getName());
    }

    private static AttributeType findAttribute(AdminGetUserResult result, String attributeName) {
        return result.getUserAttributes().stream()
                .filter(attribute -> attribute.getName().equals(attributeName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Cannot find email attribute for user: " + result.getUsername()));
    }

}
