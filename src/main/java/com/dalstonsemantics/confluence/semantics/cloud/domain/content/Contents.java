package com.dalstonsemantics.confluence.semantics.cloud.domain.content;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Contents {
    private List<Content> results;
    private int start;
    private int limit;
    private int size;
}
