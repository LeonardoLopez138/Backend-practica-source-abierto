package com.acme.nubi.platform.caterpillar.domain.aggregate;

import com.acme.nubi.platform.caterpillar.domain.model.valueobjects.OperationStatus;
import com.acme.nubi.platform.caterpillar.domain.model.valueobjects.CatMiningEquipmentUnitId;
import com.acme.nubi.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.acme.nubi.platform.shared.domain.model.valueobjects.GeoCoordinate;
import io.jsonwebtoken.lang.Strings;
import lombok.Getter;


//se ceran datos
@Getter
public class MiningEquipmentUnit extends AbstractDomainAggregateRoot<MiningEquipmentUnit> {

    //este id es unicamente para la base de datos
    //tiene que ser un int
    private Long id;
    private CatMiningEquipmentUnitId equipmentUnitId;
    private OperationStatus operationStatus;
    private String serialNumber;
    private GeoCoordinate gpsLocation;

    //                                                               ^
//se pasan los datos que se le de commandservices y se guarda arriba |
    public MiningEquipmentUnit(OperationStatus OperationStatus, String SerialNumber, GeoCoordinate GeoCoordinate) {
        //new EquipmentUnitId(), para inicilizar y crear el valor del UUID
        this.equipmentUnitId = new CatMiningEquipmentUnitId();
        this.operationStatus = OperationStatus;
        this.serialNumber = SerialNumber;
        this.gpsLocation = GeoCoordinate;
    }


    public static MiningEquipmentUnit reconstitute(Long id, CatMiningEquipmentUnitId equipmentUnitId,OperationStatus operationStatus, String serialNumber, GeoCoordinate geoCoordinate) {
        var miningEquipmentUnit = new MiningEquipmentUnit(operationStatus,serialNumber, geoCoordinate);
        miningEquipmentUnit.id = id;
        miningEquipmentUnit.serialNumber = serialNumber;
        miningEquipmentUnit.equipmentUnitId =equipmentUnitId;
        miningEquipmentUnit.operationStatus = operationStatus;
        return miningEquipmentUnit;
    }
}
