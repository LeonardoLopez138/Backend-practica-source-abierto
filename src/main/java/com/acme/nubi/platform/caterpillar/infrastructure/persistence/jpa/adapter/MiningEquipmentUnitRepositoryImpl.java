package com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.adapter;

import com.acme.nubi.platform.caterpillar.domain.aggregate.MiningEquipmentUnit;
import com.acme.nubi.platform.caterpillar.domain.repositories.MiningEquipmentUnitRepository;
import com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.assembler.MiningEquipmentUnitPersistenceAssembler;
import com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.repositories.MiningEquipmentUnitPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MiningEquipmentUnitRepositoryImpl implements MiningEquipmentUnitRepository {

    private final MiningEquipmentUnitPersistenceRepository miningEquipmentUnitPersistenceRepository;

    public MiningEquipmentUnitRepositoryImpl(
            MiningEquipmentUnitPersistenceRepository miningEquipmentUnitPersistenceRepository) {
        this.miningEquipmentUnitPersistenceRepository = miningEquipmentUnitPersistenceRepository;
    }

    @Override
    public MiningEquipmentUnit save(MiningEquipmentUnit unit) {
        var entity = MiningEquipmentUnitPersistenceAssembler.toPersistenceFromDomain(unit);
        var saved = miningEquipmentUnitPersistenceRepository.save(entity);
        return MiningEquipmentUnitPersistenceAssembler.toDomainFromPersistence(saved);
    }

    @Override                   //assegurarse que "findBySerialNumber" o sus variantes esten en repositories de domain
    public Optional<MiningEquipmentUnit> findBySerialNumber(String serialNumber) {
        return miningEquipmentUnitPersistenceRepository.findByserialNumber(serialNumber)
                .map(MiningEquipmentUnitPersistenceAssembler::toDomainFromPersistence);
    }

    @Override        //assegurarse que "existsBySerialNumber" o sus variantes esten en repositories de domain
    public boolean existsBySerialNumber(String serialNumber) {
        return miningEquipmentUnitPersistenceRepository.existsBySerialNumber(serialNumber);
    }

    @Override
    public MiningEquipmentUnit update(MiningEquipmentUnit unit) {
        return null;
    }

    @Override
    public void deleteById(Long id) {
    }

    @Override
    public MiningEquipmentUnit findById(Long id) {
        return null;
    }
}