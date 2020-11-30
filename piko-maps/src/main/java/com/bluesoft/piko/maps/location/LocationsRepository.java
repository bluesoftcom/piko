package com.bluesoft.piko.maps.location;

import com.bluesoft.piko.maps.errors.ResourceNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationsRepository extends JpaRepository<Location, String> {

    default Location find(String id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("locations", id));
    }

    List<Location> findByOwner(String username, Sort sort);

    int countByOwner(String username);

}
