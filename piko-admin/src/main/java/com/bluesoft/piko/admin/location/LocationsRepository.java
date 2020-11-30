package com.bluesoft.piko.admin.location;

import com.bluesoft.piko.admin.errors.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.Nullable;

public interface LocationsRepository extends JpaRepository<Location, String> {

    default Location find(String id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("locations", id));
    }

    @Nullable
    default Location tryFind(String id) {
        return findById(id).orElse(null);
    }

}
