package com.dalstonsemantics.confluence.semantics.cloud.domain.webhook;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventContent {
    private String id;
    private String spaceKey;
    private String title;
    private String contentType;
    private String self;
    private int version;
}