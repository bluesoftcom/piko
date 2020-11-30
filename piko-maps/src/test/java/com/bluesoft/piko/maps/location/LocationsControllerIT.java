package com.bluesoft.piko.maps.location;

import com.bluesoft.piko.maps.PikoMapsIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.bluesoft.piko.maps.location.LocationFixtures.someLocation;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocationsControllerIT extends PikoMapsIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private LocationsRepository locationsRepository;

    @Test
    void testReadLocation() throws Exception {
        // given:
        final Location location = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .build()
        );
        // when & then:
        mvc.perform(get("/locations/{locationId}", location.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(location.getId()))
                .andExpect(jsonPath("$.name").value(location.getName()))
                .andExpect(jsonPath("$.lat").value(location.getLat()))
                .andExpect(jsonPath("$.lng").value(location.getLng()))
                .andExpect(jsonPath("$.owner").value(location.getOwner()));
    }

    @Test
    void testListLocations() throws Exception {
        locationsRepository.deleteAll();
        // given:
        final Location location1 = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .name("location-1")
                        .build()
        );
        final Location location2 = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .name("location-2")
                        .build()
        );
        final Location location3 = locationsRepository.save(
                someLocation()
                        .id(UUID.randomUUID().toString())
                        .name("location-3")
                        .build()
        );

        // when & then:
        mvc.perform(get("/locations"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(location1.getId()))
                .andExpect(jsonPath("$.items[0].name").value(location1.getName()))
                .andExpect(jsonPath("$.items[0].lat").value(location1.getLat()))
                .andExpect(jsonPath("$.items[0].lng").value(location1.getLng()))
                .andExpect(jsonPath("$.items[0].owner").value(location1.getOwner()))
                .andExpect(jsonPath("$.items[1].id").value(location2.getId()))
                .andExpect(jsonPath("$.items[2].id").value(location3.getId()))
                .andExpect(jsonPath("$.items").value(hasSize(3)));
    }

}