package com.dalstonsemantics.confluence.semantics.cloud.domain.content;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Body {
    private Storage storage;
    
    @JsonCreator
    public Body(Storage storage) {
        this.storage = storage;
    }
}
