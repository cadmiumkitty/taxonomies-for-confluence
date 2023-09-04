package com.dalstonsemantics.confluence.semantics.cloud.domain.webhook.content;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Expandable {
    private String container;
    @JsonCreator
    public Expandable(String container) {
        this.container = container;
    }
}
