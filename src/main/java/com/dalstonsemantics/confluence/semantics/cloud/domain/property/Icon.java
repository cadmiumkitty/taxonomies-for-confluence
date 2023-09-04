package com.dalstonsemantics.confluence.semantics.cloud.domain.property;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder=true)
public class Icon {
    private int width;
    private int height;
    private String url;
}
