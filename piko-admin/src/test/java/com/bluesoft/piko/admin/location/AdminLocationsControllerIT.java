package com.bluesoft.piko.admin.location;

import com.bluesoft.piko.admin.PikoAdminIT;
import com.bluesoft.piko.admin.auth.LoggedUser;
import com.bluesoft.piko.admin.aws.CognitoApi;
import com.bluesoft.piko.admin.location.messaging.LocationEventType;
import com.bluesoft.piko.admin.location.messaging.LocationEventsMessageCollector;
import com.bluesoft.piko.admin.location.messaging.LocationMessage;
import com.bluesoft.piko.admin.mailcatcher.MailCatcherClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.bluesoft.piko.admin.auth.LoggedUserAuthentication.authenticatedUser;
import static com.bluesoft.piko.admin.auth.LoggedUserFixtures.someLoggedUser;
import static com.bluesoft.piko.admin.location.LocationFixtures.someLocation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminLocationsControllerIT extends PikoAdminIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private LocationsRepository locationsRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LocationEventsMessageCollector locationEventsMessageCollector;
    @Autowired
    private CognitoApi cognitoApi;
    @Autowired
    private MailCatcherClient mailCatcherClient;

    @BeforeEach
    void setUp() {
        locationEventsMessageCollector.dumpMessages();
    }

    @Test
    void testReadLocation() throws Exception {
        // given:
        final LoggedUser loggedUser = someLoggedUser("user-" + UUID.randomUUID().toString()).build();
        final Location location = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .owner(loggedUser.getUsername())
                        .build()
        );
        // when & then:
        mvc.perform(get("/locations/{locationId}", location.getId())
                .with(authenticatedUser(loggedUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(location.getId()))
                .andExpect(jsonPath("$.name").value(location.getName()))
                .andExpect(jsonPath("$.lat").value(location.getLat()))
                .andExpect(jsonPath("$.lng").value(location.getLng()))
                .andExpect(jsonPath("$.createdAt").value(location.getCreatedAt().toString()))
                .andExpect(jsonPath("$.owner").value(loggedUser.getUsername()))
                .andExpect(jsonPath("$.lastPublishedAt").value(nullValue()));
    }

    @Test
    void testPublishLocation() throws Exception {
        // given:
        final LoggedUser loggedUser = someLoggedUser("user-" + UUID.randomUUID().toString())
                .build();
        final Location location = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .owner(loggedUser.getUsername())
                        .build()
        );

        cognitoApi.mockSuccessfulAdminGetUser(loggedUser.getUsername());

        // when & then:
        mvc.perform(put("/locations/{locationId}/status", location.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"status\": \"PUBLISHED\" }")
                .with(authenticatedUser(loggedUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(LocationStatus.PUBLISHED.name()));

        // and:
        final Location foundLocation = locationsRepository.tryFind(location.getId());
        assertThat(foundLocation).isNull();

        final LocationMessage locationMessage = locationEventsMessageCollector.captureLastLocationMessage();
        assertThat(locationMessage.getId()).isEqualTo(location.getId());
        assertThat(locationMessage.getEvent()).isEqualTo(LocationEventType.LOCATION_PUBLISHED);

        final String expectedRecipient = String.format("<%s>", loggedUser.getEmail());

        final List<MailCatcherClient.MessageSummary> capturedEmails = mailCatcherClient.listMessages();
        assertThat(capturedEmails).anySatisfy(capturedEmail ->
                assertThat(capturedEmail.getRecipients()).contains(expectedRecipient)
        );
    }

    @Test
    void testListLocations() throws Exception {
        // given:
        locationsRepository.deleteAll();
        final LoggedUser loggedUser = someLoggedUser("user-" + UUID.randomUUID().toString()).build();

        final Location location1 = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .name("location-1")
                        .owner(loggedUser.getUsername())
                        .createdAt(Instant.parse("2020-03-03T12:00:00Z"))
                        .build()
        );
        final Location location2 = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .name("location-2")
                        .owner(loggedUser.getUsername())
                        .createdAt(Instant.parse("2020-03-03T13:00:00Z"))
                        .build()
        );
        final Location location3 = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .name("location-3")
                        .owner(loggedUser.getUsername())
                        .createdAt(Instant.parse("2020-03-03T14:00:00Z"))
                        .build()
        );

        // when & then:
        mvc.perform(get("/locations")
                .with(authenticatedUser(loggedUser)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(location1.getId()))
                .andExpect(jsonPath("$.items[0].name").value(location1.getName()))
                .andExpect(jsonPath("$.items[0].lat").value(location1.getLat()))
                .andExpect(jsonPath("$.items[0].lng").value(location1.getLng()))
                .andExpect(jsonPath("$.items[0].createdAt").value(location1.getCreatedAt().toString()))
                .andExpect(jsonPath("$.items[0].lastPublishedAt").value(nullValue()))
                .andExpect(jsonPath("$.items[1].id").value(location2.getId()))
                .andExpect(jsonPath("$.items[2].id").value(location3.getId()))
                .andExpect(jsonPath("$.items").value(hasSize(3)));
    }

}