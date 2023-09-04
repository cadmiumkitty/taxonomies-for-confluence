package com.dalstonsemantics.confluence.semantics.cloud.domain.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfilePicture {
    private String path;
    private int width;
    private int height;
    private boolean isDefault;
}
