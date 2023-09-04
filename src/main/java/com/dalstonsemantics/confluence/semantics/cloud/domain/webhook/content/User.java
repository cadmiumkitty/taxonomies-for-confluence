package com.dalstonsemantics.confluence.semantics.cloud.domain.webhook.content;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String accountId;
    private String accountType;    
}
