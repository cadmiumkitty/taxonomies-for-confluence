package com.dalstonsemantics.confluence.semantics.cloud.domain.addon;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class License {
    private Boolean active;
    private String type;
    private Boolean evaluation;
    private String supportEntitlementNumber;
}
