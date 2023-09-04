package com.dalstonsemantics.confluence.semantics.cloud.domain.history;

import java.util.Calendar;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class History {
    private Content content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Calendar when;
}
