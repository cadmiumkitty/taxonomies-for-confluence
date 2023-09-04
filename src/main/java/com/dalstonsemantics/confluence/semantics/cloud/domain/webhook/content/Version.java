package com.dalstonsemantics.confluence.semantics.cloud.domain.webhook.content;

import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Version {
    private User by;
    private int number;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Calendar when;
}