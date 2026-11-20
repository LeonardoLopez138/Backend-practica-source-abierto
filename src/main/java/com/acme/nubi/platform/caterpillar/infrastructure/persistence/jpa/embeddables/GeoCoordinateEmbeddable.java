package com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.embeddables;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class GeoCoordinateEmbeddable {

    //se agrega el primer dato
    @Column(name = "gps_latitude", nullable = false)
    private double lat;

    //se agrega el segundo dato
    @Column(name = "gps_longitude", nullable = false)
    private double lon;


    //los dos se juntan en el constructor
    public GeoCoordinateEmbeddable(double latitude, double longitude) {
        this.lat = latitude;
        this.lon = longitude;
    }
}