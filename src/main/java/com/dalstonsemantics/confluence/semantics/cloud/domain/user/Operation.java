package com.dalstonsemantics.confluence.semantics.cloud.domain.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Operation {
    private String operation;
    private String targetType;
}
