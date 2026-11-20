package com.acme.nubi.platform.caterpillar.interfaces.rest.transform;

import com.acme.nubi.platform.caterpillar.domain.aggregate.MiningEquipmentUnit;
import com.acme.nubi.platform.caterpillar.interfaces.rest.resources.MiningEquipmentUnitResource;

public class MiningEquipmentUnitResourceFromEntityAssembler {


    public static MiningEquipmentUnitResource toResourceFromEntity(MiningEquipmentUnit entity) {
        return new MiningEquipmentUnitResource(
                entity.getId(),
                entity.getEquipmentUnitId().equipmentId().toString(),
                entity.getSerialNumber(),
                entity.getOperationStatus().name(),
                entity.getGpsLocation().lat(),
                entity.getGpsLocation().lon());
    }
}
