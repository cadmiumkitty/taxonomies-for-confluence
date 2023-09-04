package com.dalstonsemantics.confluence.semantics.cloud.processor;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MaterializeContentGraphProcessor extends AbstractContentEventProcessor {

    public MaterializeContentGraphProcessor(
            @Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool,
            @Autowired UUIDProvider uuidp,
            @Autowired EventDispatcher dispatcher,
            @Value("${addon.repositories.taxonomy.context.content.max-size}") long contentMaxSize) {
        super(taxonomyRepositoryPool, uuidp, dispatcher, contentMaxSize);
    }

    @Override
    protected IRI getEventType() {
        return TEAM.MATERIALIZE_CONTENT_GRAPH_EVENT;
    }

    @SneakyThrows
    protected void processEvent(RepositoryConnection connection, Model eventModel, 
            IRI event, Literal clientKey, IRI contentGraph) {

        String eventClientKey = clientKey.stringValue();
    
        materializeContentGraph(connection, contentGraph, eventClientKey);
    }

    public void materializeContentGraph(RepositoryConnection connection, IRI contentGraph, String eventClientKey) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        ValueFactory vf = connection.getValueFactory();

        IRI materializedContentGraph = vf.createIRI(Namespaces.MATERIALIZED_CONTENT_GRAPH, eventClientKey);

        Update materializeContentGraph = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                INSERT {
                    GRAPH ?materializedContentGraph {
                        ?statementSubject ?statementPredicate ?statementObject .
                        ?statementSubject ?statementSubjectP ?statementSubjectO .
                        ?statementObject ?statementObjectP ?statementObjectO .
                    }
                }
                WHERE {
                    GRAPH ?contentGraph {
                        ?statement a rdf:Statement ;
                            rdf:subject ?statementSubject ;
                            rdf:predicate ?statementPredicate ;
                            rdf:object ?statementObject ;
                            team:status "current" .
                        OPTIONAL {
                            ?statementSubject ?statementSubjectP ?statementSubjectO .
                        }
                        OPTIONAL {
                            ?statementObject ?statementObjectP ?statementObjectO .
                        }    
                    }
                }
                """);
        materializeContentGraph.setBinding("contentGraph", contentGraph);
        materializeContentGraph.setBinding("materializedContentGraph", materializedContentGraph);

        connection.setIsolationLevel(IsolationLevels.READ_UNCOMMITTED);

        connection.begin();

        connection.clear(materializedContentGraph);
        materializeContentGraph.execute();

        connection.commit();
    }
}
