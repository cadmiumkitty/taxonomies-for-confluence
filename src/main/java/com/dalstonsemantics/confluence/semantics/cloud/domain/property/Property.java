package com.dalstonsemantics.confluence.semantics.cloud.domain.property;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder=true)
public class Property {

    public static final String SUBJECT = "taxonomies-for-confluence-subject";
    public static final String TYPE = "taxonomies-for-confluence-type";
    public static final String CLASS = "taxonomies-for-confluence-class";

    private String id;
    private String key;
    private Value value;
    private Version version;
}
