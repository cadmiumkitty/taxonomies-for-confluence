package com.dalstonsemantics.confluence.semantics.cloud.domain.content;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Content {
    private String id;
    private String type;
    private String spaceKey;
    private String title;
    @JsonProperty("_links")
    private Links links;
    private Version version;
    private Body body;
}
