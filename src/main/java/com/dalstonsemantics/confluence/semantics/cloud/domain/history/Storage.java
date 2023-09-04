package com.dalstonsemantics.confluence.semantics.cloud.domain.history;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Storage {
    private String value;
    @JsonCreator
    public Storage(String value) {
        this.value = value;
    }
}
