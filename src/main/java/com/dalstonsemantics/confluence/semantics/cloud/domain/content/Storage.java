package com.dalstonsemantics.confluence.semantics.cloud.domain.content;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Storage {
    private String value;
    private String representation;
}
