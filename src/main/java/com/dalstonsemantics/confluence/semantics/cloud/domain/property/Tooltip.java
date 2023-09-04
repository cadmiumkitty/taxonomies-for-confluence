package com.dalstonsemantics.confluence.semantics.cloud.domain.property;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder=true)
public class Tooltip {
    private String value;
    @JsonCreator
    public Tooltip(String value) {
        this.value = value;
    }
}
