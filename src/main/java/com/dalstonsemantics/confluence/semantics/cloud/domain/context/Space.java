package com.dalstonsemantics.confluence.semantics.cloud.domain.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Space {
    private String id;
    private String key;
}