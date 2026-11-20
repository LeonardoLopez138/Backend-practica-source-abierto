package com.acme.nubi.platform.caterpillar.interfaces.rest;

import com.acme.nubi.platform.caterpillar.application.commandservices.MiningEquipmentUnitCommandServices;
import com.acme.nubi.platform.caterpillar.interfaces.rest.resources.CreateMiningEquipmentUnitResource;
import com.acme.nubi.platform.caterpillar.interfaces.rest.resources.MiningEquipmentUnitResource;
import com.acme.nubi.platform.caterpillar.interfaces.rest.transform.CreateMiningEquipmentUnitCommandFromResourceAssembler;
import com.acme.nubi.platform.caterpillar.interfaces.rest.transform.MiningEquipmentUnitResourceFromEntityAssembler;
import com.acme.nubi.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping(value = "/api/v1/equipment-units", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Mining Equipment Units", description = "Mining equipment unit management endpoints")
public class MiningEquipmentUnitsController {

    private final MiningEquipmentUnitCommandServices miningEquipmentUnitCommandServices;

    public MiningEquipmentUnitsController(MiningEquipmentUnitCommandServices miningEquipmentUnitCommandServices) {
        this.miningEquipmentUnitCommandServices = miningEquipmentUnitCommandServices;
    }

    @PostMapping
    @Operation(summary = "Create a new mining equipment unit",
            description = "Creates a new mining equipment unit. The serial number must be unique.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Mining equipment unit created successfully",
                    content = @Content(schema = @Schema(implementation = MiningEquipmentUnitResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "A unit with the same serial number already exists")
    })
    public ResponseEntity<?> createMiningEquipmentUnit(@RequestBody CreateMiningEquipmentUnitResource resource) {
        var command = CreateMiningEquipmentUnitCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = miningEquipmentUnitCommandServices.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                MiningEquipmentUnitResourceFromEntityAssembler::toResourceFromEntity,
                HttpStatus.CREATED
        );
    }
}
