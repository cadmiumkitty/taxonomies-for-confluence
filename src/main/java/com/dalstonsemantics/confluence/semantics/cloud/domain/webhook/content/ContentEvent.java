package com.dalstonsemantics.confluence.semantics.cloud.domain.webhook.content;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentEvent {

    public static final String CONTENT_PROPERTY = "com.atlassian.confluence.plugins.confluence-content-property-storage:content-property";

    private String id;
    private String type;
    private String title;
    private Version version;
    @JsonProperty("_expandable")
    private Expandable expandable;
}
