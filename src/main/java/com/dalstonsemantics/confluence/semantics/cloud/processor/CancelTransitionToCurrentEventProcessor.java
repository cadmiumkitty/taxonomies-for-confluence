package com.dalstonsemantics.confluence.semantics.cloud.processor;

import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CancelTransitionToCurrentEventProcessor extends AbstractTaxonomyEventProcessor {

    public CancelTransitionToCurrentEventProcessor(@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool) {
        super(taxonomyRepositoryPool);
    }

    @Override
    protected IRI getEventType() {
        return TEAM.CANCEL_TRANSITION_TO_CURRENT_EVENT;
    }

    @Override
    protected Literal getTargetTaxonomyGraphStatus() {
        return TEAM.CANCELLING_TRANSITION_TO_CURRENT;
    }

    @Override
    @SneakyThrows
    protected void processEvent(RepositoryConnection connection, Model eventModel, 
            IRI event, Literal clientKey, Literal accountId, 
            IRI taxonomyVersionGraph, IRI targetTaxonomyGraph, IRI previousTaxonomyGraph, IRI contentGraph) {

        Update delete = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                DELETE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:status ?status .
                        ?targetTaxonomyGraph team:statusTransitionEvent ?statusTransitionEvent .
                        ?targetTaxonomyGraph team:impactedContent ?impactedContent .
                        ?targetTaxonomyGraph team:impactedContentCount ?impactedContentCount .
                    }
                }
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:status ?status .
                        ?targetTaxonomyGraph team:statusTransitionEvent ?statusTransitionEvent .
                        OPTIONAL { ?targetTaxonomyGraph team:impactedContent ?impactedContent }
                        OPTIONAL { ?targetTaxonomyGraph team:impactedContentCount ?impactedContentCount }
                    }
                }
                """);
        delete.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        delete.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        Update insert = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:status ?updatedStatus .
                    }
                }
                WHERE {
                }
                """);
        insert.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insert.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insert.setBinding("updatedStatus", TEAM.DRAFT);

        connection.begin();

        delete.execute();
        insert.execute();

        connection.commit();
    }
}