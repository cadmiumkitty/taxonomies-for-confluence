package com.dalstonsemantics.confluence.semantics.cloud.provider;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

@Component
public class LocalDateTimeProvider {

    public LocalDateTime nowInUTC() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }
}
