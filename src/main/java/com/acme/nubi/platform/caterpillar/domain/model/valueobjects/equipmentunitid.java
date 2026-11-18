package com.acme.nubi.platform.caterpillar.domain.model.valueobjects;


import java.util.UUID;

//se crea la clase
public record equipmentunitid(UUID equipmentId) {

    //se crea el constructor ENCARGADO, de generarle un UUID
    public equipmentunitid(){
        this(UUID.randomUUID());
        //"this" se refiere a que se esta dando el valor de (UUID.randomUUID()) hacia CatMinigEquimentUnitId
    }

    //constructor encargado de verificar si es que es nulo o vacio
    public equipmentunitid{
        if(equipmentId == null || equipmentId.equals(new UUID(0L,0L))) {
            throw new IllegalArgumentException("Equipment unit id cannot be null or empty");
        }
    }
}
