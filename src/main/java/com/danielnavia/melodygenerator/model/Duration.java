package com.danielnavia.melodygenerator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

@Getter
public enum Duration {
    SIXTEENTH( 1),
    EIGHTH(2),
    QUARTER(4),
    HALF(8);

    @JsonIgnore
    private final int units;

    Duration(int units) {
        this.units = units;
    }
}