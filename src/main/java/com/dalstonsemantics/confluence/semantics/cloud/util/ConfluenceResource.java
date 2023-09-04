package com.dalstonsemantics.confluence.semantics.cloud.util;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfluenceResource {
    private boolean valid;
    private String spaceKey;
    private String contentType;
    private String contentId;
    private String anchor;
}