package com.acme.nubi.platform.caterpillar.infrastructure.persistance.jpa.converters;
import com.acme.nubi.platform.caterpillar.domain.model.valueobjects.CatMiningEquipmentUnitId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


import java.util.UUID;

@Converter(autoApply = false)
public class CatMiningEquipmentUnitIdPersistanceConverter implements AttributeConverter<CatMiningEquipmentUnitId, UUID> {

    @Override
    public UUID convertToDatabaseColumn(CatMiningEquipmentUnitId attribute) {
        return attribute == null ? null : attribute.equipmentId();
    }

    @Override
    public CatMiningEquipmentUnitId convertToEntityAttribute(UUID dbData) {
        return dbData == null ? null : new CatMiningEquipmentUnitId(dbData);
    }
}