package com.dalstonsemantics.confluence.semantics.cloud.domain.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Confluence {
    private Content content;
    private Space space;
}