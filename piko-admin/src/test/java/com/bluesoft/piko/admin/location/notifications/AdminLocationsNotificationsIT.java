package com.bluesoft.piko.admin.location.notifications;

import com.bluesoft.piko.admin.PikoAdminIT;
import com.bluesoft.piko.admin.aws.CognitoApi;
import com.bluesoft.piko.admin.location.json.DetailedJsonLocation;
import com.bluesoft.piko.admin.mailcatcher.MailCatcherClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.UUID;

import static com.bluesoft.piko.admin.location.LocationFixtures.someDetailedJsonLocation;
import static com.bluesoft.piko.admin.location.LocationFixtures.someJsonLocation;
import static com.bluesoft.piko.admin.mailcatcher.MailCatcherClient.MailcatcherMessage;
import static com.bluesoft.piko.admin.mailcatcher.MailCatcherClient.MessageSummary;
import static org.assertj.core.api.Assertions.assertThat;

class AdminLocationsNotificationsIT extends PikoAdminIT {

    @Autowired
    private AdminLocationsNotifications adminLocationsNotifications;
    @Autowired
    private CognitoApi cognitoApi;
    @Autowired
    private MailCatcherClient mailCatcherClient;
    @Autowired
    private NotificationsProperties notificationsProperties;

    @Test
    void testSendLocationPublishedNotification() throws Exception {
        // given:
        final String username = "johndoe-" + UUID.randomUUID().toString();
        final String recipient = String.format("<%s@email.test>", username);
        cognitoApi.mockSuccessfulAdminGetUser(username);

        final DetailedJsonLocation location = someDetailedJsonLocation()
                .location(
                        someJsonLocation()
                                .name("Corn Flower Museum")
                                .build()
                )
                .owner(username)
                .build();

        // when:
        adminLocationsNotifications.sendLocationPublishedEmail(location);

        // then:
        final List<MessageSummary> messages = mailCatcherClient.listMessages();
        final MessageSummary foundMessage = findByRecipient(messages, recipient);
        assertThat(foundMessage).isNotNull();

        final MailcatcherMessage message = mailCatcherClient.fetchMessage(foundMessage.getId());
        assertThat(message.getRecipients()).contains(recipient);
        assertThat(message.getSender()).isEqualTo(String.format("<%s>", notificationsProperties.getFromAddress()));
        assertThat(message.getSubject()).isEqualTo("Your location Corn Flower Museum was published");
        assertThat(message.getSource())
                .contains(username)
                .contains(location.getLocation().getName());
    }

    @Nullable
    private static MessageSummary findByRecipient(List<MessageSummary> messages, String recipientEmail) {
        return messages.stream()
                .filter(message -> message.getRecipients().contains(recipientEmail))
                .findFirst()
                .orElse(null);
    }

    @AfterEach
    void tearDown() {
        cognitoApi.reset();
    }
}