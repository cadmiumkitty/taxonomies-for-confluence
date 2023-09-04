package com.dalstonsemantics.confluence.semantics.cloud.provider;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class UUIDProvider {

    public UUID randomUUID() {
        return UUID.randomUUID();
    }
}
