package com.acme.nubi.platform.caterpillar.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "CreateMiningEquipmentUnitRequest",
        description = "Request payload for creating a new mining equipment unit",
        example = "{\"serialNumber\": \"SN-0001\", \"operationStatus\": \"ACTIVE\", \"gpsLatitude\": -12.0464, \"gpsLongitude\": -77.0428}"
)
public record CreateMiningEquipmentUnitResource(
        @Schema(description = "Serial number of the mining equipment unit", example = "SN-0001")
        String serialNumber,

        @Schema(description = "Operation status. Allowed values: ACTIVE, IN_MAINTENANCE, INACTIVE", example = "ACTIVE")
        String operationStatus,

        @Schema(description = "GPS latitude, between -90 and 90", example = "-12.0464")
        double gpsLatitude,

        @Schema(description = "GPS longitude, between -180 and 180", example = "-77.0428")
        double gpsLongitude
) {

    public CreateMiningEquipmentUnitResource {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("Serial number is required");
        }
        if (operationStatus == null || operationStatus.isBlank()) {
            throw new IllegalArgumentException("Operation status is required");
        }
    }
}
