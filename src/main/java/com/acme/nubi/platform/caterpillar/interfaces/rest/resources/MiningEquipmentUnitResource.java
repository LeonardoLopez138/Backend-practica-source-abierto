package com.acme.nubi.platform.caterpillar.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(
        name = "MiningEquipmentUnitResponse",
        description = "Mining equipment unit information response",
        example = "{\"id\": 1, \"equipmentUnitId\": \"3f2b8c1e-9a4d-4e7b-8c21-5d6e7f8a9b0c\", \"serialNumber\": \"SN-0001\", \"operationStatus\": \"ACTIVE\", \"gpsLatitude\": -12.0464, \"gpsLongitude\": -77.0428}"
)
public record MiningEquipmentUnitResource(
        @Schema(description = "Database identifier of the unit", example = "1")
        Long id,

        @Schema(description = "Business identifier of the unit (UUID)", example = "3f2b8c1e-9a4d-4e7b-8c21-5d6e7f8a9b0c")
        String equipmentUnitId,

        @Schema(description = "Serial number of the mining equipment unit", example = "SN-0001")
        String serialNumber,

        @Schema(description = "Operation status", example = "ACTIVE")
        String operationStatus,

        @Schema(description = "GPS latitude", example = "-12.0464")
        double gpsLatitude,

        @Schema(description = "GPS longitude", example = "-77.0428")
        double gpsLongitude
) {
}
