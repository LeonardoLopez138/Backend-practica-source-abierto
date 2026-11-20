package com.acme.nubi.platform.caterpillar.interfaces.rest.transform;

import com.acme.nubi.platform.caterpillar.application.commands.CreateMiningEquipmentUnitCommand;
import com.acme.nubi.platform.caterpillar.interfaces.rest.resources.CreateMiningEquipmentUnitResource;


public class CreateMiningEquipmentUnitCommandFromResourceAssembler {

    public static CreateMiningEquipmentUnitCommand toCommandFromResource(CreateMiningEquipmentUnitResource resource) {
        return new CreateMiningEquipmentUnitCommand(
                resource.serialNumber(),
                resource.operationStatus(),
                resource.gpsLatitude(),
                resource.gpsLongitude());
    }
}
