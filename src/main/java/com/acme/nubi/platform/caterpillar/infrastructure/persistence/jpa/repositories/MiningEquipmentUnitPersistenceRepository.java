package com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.repositories;

import com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.entities.MiningEquipmentUnitPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MiningEquipmentUnitPersistenceRepository extends JpaRepository<MiningEquipmentUnitPersistenceEntity, Long> {

    Optional<MiningEquipmentUnitPersistenceEntity> findByserialNumber (String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    boolean existsBySerialNumberAndIdIsNot(String serialNumber, Long id);

    //en caso de que sean 2 valores
    //Optional<MiningEquipmentUnitPersistenceEntity> findByGpsLocationLatAndGpsLocationLon(double lat, double lon);
}
