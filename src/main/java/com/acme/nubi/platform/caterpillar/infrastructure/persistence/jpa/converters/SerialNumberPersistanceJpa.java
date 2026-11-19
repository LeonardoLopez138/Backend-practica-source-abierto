package com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.converters;

import com.acme.nubi.platform.caterpillar.domain.model.valueobjects.SerialNumber;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SerialNumberPersistanceJpa  implements AttributeConverter<SerialNumber, String> {

    @Override
    public String convertToDatabaseColumn(SerialNumber attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public SerialNumber convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new SerialNumber(dbData);
    }
}
