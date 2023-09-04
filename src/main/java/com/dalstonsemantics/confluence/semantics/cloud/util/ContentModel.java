package com.dalstonsemantics.confluence.semantics.cloud.util;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContentModel {
    private Model statements;
    private List<IRI> contentIRIs;
    private Model content;
    private List<IRI> agentIRIs;
    private Model agent;
}
