package com.dalstonsemantics.confluence.semantics.cloud.util;

import org.eclipse.rdf4j.model.IRI;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@ToString
@Builder
@Data
public class TableIdMacroIdIRI {
    private String tableId;
    private String macroId;
    private IRI predicate;
    private boolean identifier;
    private boolean empty;
}
