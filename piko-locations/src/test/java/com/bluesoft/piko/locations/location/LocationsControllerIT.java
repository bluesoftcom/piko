package com.bluesoft.piko.locations.location;

import com.bluesoft.piko.locations.PikoLocationsIT;
import com.bluesoft.piko.locations.auth.LoggedUser;
import com.bluesoft.piko.locations.location.messaging.LocationEventType;
import com.bluesoft.piko.locations.location.messaging.LocationEventsMessageCollector;
import com.bluesoft.piko.locations.location.messaging.LocationMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.UUID;

import static com.bluesoft.piko.locations.auth.LoggedUserAuthentication.authenticatedUser;
import static com.bluesoft.piko.locations.auth.LoggedUserFixtures.someLoggedUser;
import static com.bluesoft.piko.locations.location.LocationFixtures.someLocation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocationsControllerIT extends PikoLocationsIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private LocationsRepository locationsRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LocationEventsMessageCollector locationEventsMessageCollector;

    @BeforeEach
    void setUp() {
        locationEventsMessageCollector.dumpMessages();
    }

    @Test
    void testCreateLocation() throws Exception {
        // given:
        final LoggedUser loggedUser = someLoggedUser("user-" + UUID.randomUUID().toString()).build();

        // when & then:
        final MvcResult result = mvc.perform(post("/locations")
                .with(authenticatedUser(loggedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "  \"name\": \"Guadalajara Mansion\",\n" +
                        "  \"lat\": 23.234,\n" +
                        "  \"lng\": -45.3\n" +
                        "}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.name").value("Guadalajara Mansion"))
                .andExpect(jsonPath("$.status").value(LocationStatus.DRAFT.name()))
                .andExpect(jsonPath("$.lat").value(23.234))
                .andExpect(jsonPath("$.lng").value(-45.3))
                .andExpect(jsonPath("$.createdAt").isString())
                .andExpect(jsonPath("$.owner").value(loggedUser.getUsername()))
                .andExpect(jsonPath("$.lastPublishedAt").value(nullValue()))
                .andReturn();


        final JsonNode location = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                JsonNode.class
        );

        final String locationId = location.get("id").asText();
        final Location createdLocation = locationsRepository.find(locationId);
        assertThat(createdLocation).isNotNull();
    }

    @Test
    void testUpdateLocation() throws Exception {
        // given:
        final LoggedUser loggedUser = someLoggedUser("user-" + UUID.randomUUID().toString()).build();
        final Location location = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .name("location-1")
                        .status(LocationStatus.AWAITING_PUBLICATION)
                        .owner(loggedUser.getUsername())
                        .createdAt(Instant.parse("2020-03-03T12:00:00Z"))
                        .build()
        );
        // when & then:
        mvc.perform(put("/locations/{locationId}", location.getId())
                .with(authenticatedUser(loggedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "  \"name\": \"Guadalajara Mansion\",\n" +
                        "  \"lat\": 23.234,\n" +
                        "  \"lng\": -45.3\n" +
                        "}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.name").value("Guadalajara Mansion"))
                .andExpect(jsonPath("$.status").value(LocationStatus.DRAFT.name()))
                .andExpect(jsonPath("$.lat").value(23.234))
                .andExpect(jsonPath("$.lng").value(-45.3))
                .andExpect(jsonPath("$.createdAt").isString())
                .andExpect(jsonPath("$.owner").value(loggedUser.getUsername()))
                .andExpect(jsonPath("$.lastPublishedAt").value(nullValue()));

        final Location updatedLocation = locationsRepository.find(location.getId());
        assertThat(updatedLocation.getLat()).isEqualTo(23.234);
        assertThat(updatedLocation.getLng()).isEqualTo(-45.3);
        assertThat(updatedLocation.getStatus()).isEqualTo(LocationStatus.DRAFT);

        final LocationMessage lastMessage = locationEventsMessageCollector.captureLastLocationMessage();
        assertThat(lastMessage.getId()).isEqualTo(location.getId());
        assertThat(lastMessage.getEvent()).isEqualTo(LocationEventType.PUBLICATION_REQUEST_WITHDRAWN);
    }

    @Test
    void testUpdateLocationStatus() throws Exception {
        // given:
        final LoggedUser loggedUser = someLoggedUser("user-" + UUID.randomUUID().toString()).build();
        final Location location = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .name("location-1")
                        .status(LocationStatus.DRAFT)
                        .owner(loggedUser.getUsername())
                        .createdAt(Instant.parse("2020-03-03T12:00:00Z"))
                        .build()
        );
        // when & then:
        mvc.perform(put("/locations/{locationId}/status", location.getId())
                .with(authenticatedUser(loggedUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "  \"status\": \"AWAITING_PUBLICATION\"" +
                        "}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(location.getId()))
                .andExpect(jsonPath("$.name").value(location.getName()))
                .andExpect(jsonPath("$.status").value(LocationStatus.AWAITING_PUBLICATION.name()))
                .andExpect(jsonPath("$.lat").value(location.getLat()))
                .andExpect(jsonPath("$.lng").value(location.getLng()))
                .andExpect(jsonPath("$.createdAt").value(location.getCreatedAt().toString()))
                .andExpect(jsonPath("$.owner").value(loggedUser.getUsername()))
                .andExpect(jsonPath("$.lastPublishedAt").value(nullValue()));

        final Location updatedLocation = locationsRepository.find(location.getId());
        assertThat(updatedLocation.getStatus()).isEqualTo(LocationStatus.AWAITING_PUBLICATION);

        final LocationMessage lastMessage = locationEventsMessageCollector.captureLastLocationMessage();
        assertThat(lastMessage.getId()).isEqualTo(location.getId());
        assertThat(lastMessage.getEvent()).isEqualTo(LocationEventType.PUBLICATION_REQUEST_ISSUED);
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
                .andExpect(jsonPath("$.status").value(location.getStatus().name()))
                .andExpect(jsonPath("$.lat").value(location.getLat()))
                .andExpect(jsonPath("$.lng").value(location.getLng()))
                .andExpect(jsonPath("$.createdAt").value(location.getCreatedAt().toString()))
                .andExpect(jsonPath("$.owner").value(loggedUser.getUsername()))
                .andExpect(jsonPath("$.lastPublishedAt").value(nullValue()));
    }

    @Test
    void testListLocations() throws Exception {
        // given:
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
                .andExpect(jsonPath("$.items[0].status").value(location1.getStatus().name()))
                .andExpect(jsonPath("$.items[0].createdAt").value(location1.getCreatedAt().toString()))
                .andExpect(jsonPath("$.items[0].lastPublishedAt").value(nullValue()))
                .andExpect(jsonPath("$.items[1].id").value(location2.getId()))
                .andExpect(jsonPath("$.items[2].id").value(location3.getId()))
                .andExpect(jsonPath("$.items").value(hasSize(3)));
    }

}