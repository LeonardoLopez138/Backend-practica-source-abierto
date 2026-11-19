package com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.embeddables;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class GeoCoordinateEmbeddable {

    @Column(name = "gps_latitude", nullable = false)
    private double lat;

    @Column(name = "gps_longitude", nullable = false)
    private double lon;

    public GeoCoordinateEmbeddable(double latitude, double longitude) {
        this.lat = latitude;
        this.lon = longitude;
    }
}