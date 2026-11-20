package com.acme.nubi.platform.caterpillar.application.commandservices;

import com.acme.nubi.platform.caterpillar.application.commands.CreateMiningEquipmentUnitCommand;
import com.acme.nubi.platform.caterpillar.domain.aggregate.MiningEquipmentUnit;
import com.acme.nubi.platform.shared.application.result.ApplicationError;
import com.acme.nubi.platform.shared.application.result.Result;

public interface MiningEquipmentUnitCommandServices {
    Result<MiningEquipmentUnit, ApplicationError> handle(CreateMiningEquipmentUnitCommand command);
}