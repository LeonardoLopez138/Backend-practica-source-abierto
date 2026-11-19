package com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.entities;

import com.acme.nubi.platform.caterpillar.domain.model.valueobjects.CatMiningEquipmentUnitId;
import com.acme.nubi.platform.caterpillar.domain.model.valueobjects.OperationStatus;
import com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.converters.CatMiningEquipmentUnitIdPersistanceConverter;
import com.acme.nubi.platform.caterpillar.infrastructure.persistence.jpa.embeddables.GeoCoordinateEmbeddable;
import com.acme.nubi.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//entidad
@Entity
//nombre de la tabla de base de datos
//mismo nombre que el aggregate, es el que engloba todas las cosas
@Table(name = "mining_equipment_units")
@Getter
@Setter
@NoArgsConstructor
public class MiningEquipmentUnitPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = CatMiningEquipmentUnitIdPersistanceConverter.class)
    @Column(name = "equipment_unit_id", nullable = false, unique = true)//unique se pone unicamente cuando no quieras que se repita algo en la base de datos
    private CatMiningEquipmentUnitId equipmentUnitId;

    @Column(name = "serial_number", nullable = false)
    private String serialNumber;
    //se pone string porque es un dato unico de una variable primitiva

    @Enumerated(EnumType.STRING) //para los que son enum se pasen a string y se ingresen con normalidad en la tabla
    @Column(nullable = false) //que no pueda ser null
    private OperationStatus operationStatus;

    @Embedded //es para cuando hay 2 datos o mas
    private GeoCoordinateEmbeddable gpsLocation;


    //@OneToMany(mappedBy = "MiningEquipmentUnitPersistanceEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<LearningPathItemPersistenceEntity> learningPathItems = new ArrayList<>();
}
