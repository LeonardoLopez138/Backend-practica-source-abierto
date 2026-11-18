package com.acme.nubi.platform.shared.domain.model.valueobjects;

public record GeoCoordinate(double lat, double lon) {
public GeoCoordinate{
    if(!(90>=lat && lat>=-90))
    {
        throw new IllegalArgumentException("No puede sobrepasar los 90 y -90");
    }
    if(!(180>=lon && lon>=-180))
    {
        throw new IllegalArgumentException("No puede sobrepasar los 180 y -180");

    }
}
}
