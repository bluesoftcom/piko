package com.bluesoft.piko.locations.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LocationStatus status;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant lastPublishedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location location = (Location) o;
        return getId().equals(location.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
