package com.dalstonsemantics.confluence.semantics.cloud.domain.content;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Links {
    private String base;
    private String webui;
}
