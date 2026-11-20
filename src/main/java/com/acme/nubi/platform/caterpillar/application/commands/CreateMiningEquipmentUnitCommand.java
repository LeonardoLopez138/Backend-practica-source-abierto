package com.acme.nubi.platform.caterpillar.application.commands;

import com.acme.nubi.platform.shared.application.result.ApplicationError;
import com.acme.nubi.platform.shared.application.result.Result;


//se pasan los datos que unicamente envia el usuario
//por ejemplo el id y el uuid lo genera el backend, no se ingresa al command
public record  CreateMiningEquipmentUnitCommand(String serialNumber, String operationStatus, double lat, double lon) {
    public CreateMiningEquipmentUnitCommand {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new IllegalArgumentException("title cannot be null or blank");
        }
        if (operationStatus == null || operationStatus.isBlank()) {
            throw new IllegalArgumentException("description cannot be null or blank");
        }
    }
}