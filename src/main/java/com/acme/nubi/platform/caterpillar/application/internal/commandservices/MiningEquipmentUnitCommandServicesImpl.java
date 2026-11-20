package com.acme.nubi.platform.caterpillar.application.internal.commandservices;

import com.acme.nubi.platform.caterpillar.application.commands.CreateMiningEquipmentUnitCommand;
import com.acme.nubi.platform.caterpillar.application.commandservices.MiningEquipmentUnitCommandServices;
import com.acme.nubi.platform.caterpillar.domain.aggregate.MiningEquipmentUnit;
import com.acme.nubi.platform.caterpillar.domain.model.valueobjects.OperationStatus;
import com.acme.nubi.platform.caterpillar.domain.repositories.MiningEquipmentUnitRepository;
import com.acme.nubi.platform.shared.application.result.ApplicationError;
import com.acme.nubi.platform.shared.application.result.Result;
import com.acme.nubi.platform.shared.domain.model.valueobjects.GeoCoordinate;
import org.springframework.stereotype.Service;


@Service
public class MiningEquipmentUnitCommandServicesImpl implements MiningEquipmentUnitCommandServices {

    private final MiningEquipmentUnitRepository miningEquipmentUnitRepository;

    public MiningEquipmentUnitCommandServicesImpl(MiningEquipmentUnitRepository miningEquipmentUnitRepository) {
        this.miningEquipmentUnitRepository = miningEquipmentUnitRepository;
    }

    @Override
    public Result<MiningEquipmentUnit, ApplicationError> handle(CreateMiningEquipmentUnitCommand command) {
        if (miningEquipmentUnitRepository.existsBySerialNumber(command.serialNumber())) {
            return Result.failure(ApplicationError.conflict("MiningEquipmentUnit",
                    "Serial number '%s' already exists".formatted(command.serialNumber())));
        }
        MiningEquipmentUnit unit;
        try {
            unit = new MiningEquipmentUnit(
                    OperationStatus.valueOf(command.operationStatus()),
                    command.serialNumber(),
                    new GeoCoordinate(command.lat(), command.lon()));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("mining-equipment-unit", e.getMessage()));
        }
        try {
            return Result.success(miningEquipmentUnitRepository.save(unit));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("create-mining-equipment-unit", e.getMessage()));
        }
    }
}