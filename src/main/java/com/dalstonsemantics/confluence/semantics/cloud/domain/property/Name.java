package com.dalstonsemantics.confluence.semantics.cloud.domain.property;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder=true)
public class Name {
    private String value;
    @JsonCreator
    public Name(String value) {
        this.value = value;
    }
}
