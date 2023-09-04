package com.dalstonsemantics.confluence.semantics.cloud.domain.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Content {
    private String id;
    private String type;
    private String version;
}