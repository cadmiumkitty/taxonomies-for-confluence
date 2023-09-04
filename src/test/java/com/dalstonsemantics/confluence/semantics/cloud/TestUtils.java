package com.dalstonsemantics.confluence.semantics.cloud;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.mockito.Mockito;

import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;

public class TestUtils {
    
    public static final String getRepositoryContent(RepositoryConnection connection, IRI graph) {

        String query = 
                """
                CONSTRUCT {
                    ?s ?p ?o .
                }
                WHERE { 
                    GRAPH ?graph {
                        ?s ?p ?o .
                    }
                }
                """;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, baos);
        GraphQuery graphQuery = connection.prepareGraphQuery(QueryLanguage.SPARQL, query);
        graphQuery.setBinding("graph", graph);
        graphQuery.evaluate(writer);
        return new String(baos.toByteArray());
    }
}
