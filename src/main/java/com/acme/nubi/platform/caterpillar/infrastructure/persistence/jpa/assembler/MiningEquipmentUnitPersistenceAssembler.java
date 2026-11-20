package com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.assembler;

import com.acme.nubi.platform.caterpillar.domain.aggregate.MiningEquipmentUnit;
import com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.embeddables.GeoCoordinateEmbeddable;
import com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.entities.MiningEquipmentUnitPersistenceEntity;
import com.acme.nubi.platform.shared.domain.model.valueobjects.GeoCoordinate;

public final class MiningEquipmentUnitPersistenceAssembler {

    private MiningEquipmentUnitPersistenceAssembler() {
    }

    public static MiningEquipmentUnit toDomainFromPersistence(MiningEquipmentUnitPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return MiningEquipmentUnit.reconstitute(
                entity.getId(),
                entity.getEquipmentUnitId(),
                entity.getOperationStatus(),
                entity.getSerialNumber(),
                new GeoCoordinate(
                        entity.getGpsLocation().getLat(),
                        entity.getGpsLocation().getLon()));
    }

    public static MiningEquipmentUnitPersistenceEntity toPersistenceFromDomain(MiningEquipmentUnit unit) {
        if (unit == null) {
            return null;
        }
        var entity = new MiningEquipmentUnitPersistenceEntity();
        //id base de datos
        if (unit.getId() != null) {
            entity.setId(unit.getId());
        }
        entity.setEquipmentUnitId(unit.getEquipmentUnitId());
        entity.setSerialNumber(unit.getSerialNumber());
        entity.setOperationStatus(unit.getOperationStatus());
        entity.setGpsLocation(new GeoCoordinateEmbeddable(
                unit.getGpsLocation().lat(),
                unit.getGpsLocation().lon()));
        return entity;
    }
}