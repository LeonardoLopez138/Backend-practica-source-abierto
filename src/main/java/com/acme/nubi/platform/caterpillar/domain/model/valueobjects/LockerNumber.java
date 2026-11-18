package com.acme.nubi.platform.caterpillar.domain.model.valueobjects;

import java.util.regex.Pattern;

public record LockerNumber(String value) {
    private static final Pattern PATTER_LOCKED_REGEX =
            Pattern.compile("^L[0-9]{3}$");

    public LockerNumber{
        if(value == null || value.isBlank()){
            throw new IllegalArgumentException("No puede ser ni nulo ni blanco");
        }
        if(!PATTER_LOCKED_REGEX.matcher(value).matches())
        {
            throw new IllegalArgumentException("No coincide con la entrada necesaria");
        }
    }
}
