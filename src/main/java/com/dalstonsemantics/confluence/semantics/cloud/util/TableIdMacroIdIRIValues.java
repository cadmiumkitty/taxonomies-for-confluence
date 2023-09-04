package com.dalstonsemantics.confluence.semantics.cloud.util;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@ToString
@Builder
@Data
public class TableIdMacroIdIRIValues {
    private String tableId;
    private String macroId;
    private IRI predicate;
    private List<Value> objects;
    private boolean identifier;
    private boolean empty;
}
