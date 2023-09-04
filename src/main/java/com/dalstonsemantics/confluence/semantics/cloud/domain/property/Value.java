package com.dalstonsemantics.confluence.semantics.cloud.domain.property;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder=true)
public class Value {
    private String uri;
    @JsonInclude(Include.NON_EMPTY)
    private String notation;
    private Name name;
    private Tooltip tooltip;
    private Icon icon;
}
