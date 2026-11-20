package com.acme.nubi.platform.caterpillar.domain.repositories;

import com.acme.nubi.platform.caterpillar.domain.aggregate.MiningEquipmentUnit;
import com.acme.nubi.platform.caterpillar.domain.model.valueobjects.OperationStatus;

import java.util.Optional;

//a la izquierda se pone tu dato aggregate y a la derecha un long normal
//(AGGREGATE, Long)
public interface MiningEquipmentUnitRepository extends ICrudService<MiningEquipmentUnit,Long> {
    Optional<MiningEquipmentUnit> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

}


//de aqui salen los metodos para el adapter