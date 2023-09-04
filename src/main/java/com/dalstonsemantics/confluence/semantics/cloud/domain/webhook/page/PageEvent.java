package com.dalstonsemantics.confluence.semantics.cloud.domain.webhook.page;

import com.dalstonsemantics.confluence.semantics.cloud.domain.webhook.EventContent;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageEvent {
    private String userAccountId;
    private String accountType;
    private EventContent page;
}
