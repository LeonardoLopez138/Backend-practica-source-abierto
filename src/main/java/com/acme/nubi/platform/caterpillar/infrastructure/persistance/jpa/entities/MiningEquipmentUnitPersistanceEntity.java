package com.acme.nubi.platform.caterpillar.infrastructure.persistance.jpa.entities;

import com.acme.nubi.platform.caterpillar.domain.model.valueobjects.CatMiningEquipmentUnitId;
import com.acme.nubi.platform.caterpillar.domain.model.valueobjects.OperationStatus;
import com.acme.nubi.platform.shared.domain.model.valueobjects.GeoCoordinate;
import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "MiningEquipmentUnitPersistanceEntity")
@Getter
@Setter
@NoArgsConstructor
public class MiningEquipmentUnitPersistanceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private CatMiningEquipmentUnitId equipmentUnitId;

    @Column(nullable = false)
    private OperationStatus operationStatus;

    @Column(nullable = false)
    private String serialNumber;

    @Column(nullable = false)
    private GeoCoordinate gpsLocation;

    //@OneToMany(mappedBy = "MiningEquipmentUnitPersistanceEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<LearningPathItemPersistenceEntity> learningPathItems = new ArrayList<>();
}
