package com.dalstonsemantics.confluence.semantics.cloud.domain.addon;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddOn {
    private String key;
    private String version;
    private String state;
    private License license;
}
