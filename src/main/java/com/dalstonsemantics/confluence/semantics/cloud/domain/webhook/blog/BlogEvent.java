package com.dalstonsemantics.confluence.semantics.cloud.domain.webhook.blog;

import com.dalstonsemantics.confluence.semantics.cloud.domain.webhook.EventContent;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlogEvent {
    private String userAccountId;
    private String accountType;    
    private EventContent blog;
}
