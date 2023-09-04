package com.dalstonsemantics.confluence.semantics.cloud.processor;

import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract event processor for all taxonomy lifecycle events (e.g. import, clear).
 */
@Slf4j
public abstract class AbstractTaxonomyEventProcessor implements EventProcessor {

    private Rdf4jRepositoryPool taxonomyRepositoryPool;

    public AbstractTaxonomyEventProcessor(Rdf4jRepositoryPool taxonomyRepositoryPool) {
        this.taxonomyRepositoryPool = taxonomyRepositoryPool;
    }

    public void onEvent(Model eventModel) {

        IRI event = (IRI)eventModel.getStatements(null, RDF.TYPE, getEventType()).iterator().next().getSubject();
        Literal clientKey = (Literal)eventModel.getStatements(event, TEAM.CLIENT_KEY, null).iterator().next().getObject();
        Literal accountId = (Literal)eventModel.getStatements(event, TEAM.ACCOUNT_ID, null).iterator().next().getObject();
        IRI targetTaxonomyGraph = (IRI)eventModel.getStatements(event, TEAM.TARGET_TAXONOMY_GRAPH, null).iterator().next().getObject();

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(clientKey.stringValue());

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            connection.setIsolationLevel(IsolationLevels.READ_UNCOMMITTED);

            ValueFactory vf = taxonomyRepository.getValueFactory();
            IRI taxonomyVersionGraph = vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, clientKey.stringValue());
            IRI contentGraph = vf.createIRI(Namespaces.CONTENT_GRAPH, clientKey.stringValue());

            TupleQuery selectTargetTaxonomy = SPARQLFactory.tupleQueryWithConnection(
                    connection,
                    """
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    SELECT ?previousTaxonomyGraph ?statusTransitionEvent
                    WHERE {
                        GRAPH ?taxonomyVersionGraph {
                            ?taxonomyGraph a team:TaxonomyGraph ;
                                team:previousTaxonomyGraph ?previousTaxonomyGraph ;
                                team:statusTransitionEvent ?statusTransitionEvent ;
                                team:status ?status .
                        }
                    }
                    """);
            selectTargetTaxonomy.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
            selectTargetTaxonomy.setBinding("taxonomyGraph", targetTaxonomyGraph);
            selectTargetTaxonomy.setBinding("status", getTargetTaxonomyGraphStatus());

             try (TupleQueryResult selectTargetTaxonomyResult = selectTargetTaxonomy.evaluate()) {

                if (selectTargetTaxonomyResult.hasNext()) {

                    BindingSet targetTaxonomyBindingSet = selectTargetTaxonomyResult.next();

                    Value statusTransitionEvent = targetTaxonomyBindingSet.getValue("statusTransitionEvent");
                    IRI previousTaxonomyGraph = (IRI)targetTaxonomyBindingSet.getValue("previousTaxonomyGraph");

                    log.info("Target status transition event: {}", statusTransitionEvent.stringValue());

                    if (statusTransitionEvent.equals(event)) {

                        log.info("Processing: {}", getEventType());

                        processEvent(connection, eventModel, event, clientKey, accountId, taxonomyVersionGraph, targetTaxonomyGraph, previousTaxonomyGraph, contentGraph);

                        log.info("Processing complete: {}", getEventType());
                    } else {

                        log.warn("Status transition event on the taxonomy graph does not match. Expected: {}. Found: {}. Skipping.", 
                                statusTransitionEvent.stringValue(), event);
                    }
                } else {
                    
                    log.warn("No matching taxonomy graphs found. Skipping.");
                }
            }
        }
    }

    public void onError(Model eventModel, Throwable th) {

        IRI event = (IRI)eventModel.getStatements(null, RDF.TYPE, getEventType()).iterator().next().getSubject();
        Literal clientKey = (Literal)eventModel.getStatements(event, TEAM.CLIENT_KEY, null).iterator().next().getObject();
        Literal accountId = (Literal)eventModel.getStatements(event, TEAM.ACCOUNT_ID, null).iterator().next().getObject();
        IRI targetTaxonomyGraph = (IRI)eventModel.getStatements(event, TEAM.TARGET_TAXONOMY_GRAPH, null).iterator().next().getObject();

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(clientKey.stringValue());

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            connection.setIsolationLevel(IsolationLevels.READ_UNCOMMITTED);

            ValueFactory vf = taxonomyRepository.getValueFactory();
            IRI taxonomyVersionGraph = vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, clientKey.stringValue());

            Update delete = SPARQLFactory.updateWithConnection(
                    connection,
                    """
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    DELETE {
                        GRAPH ?taxonomyVersionGraph {
                            ?taxonomyGraph a team:TaxonomyGraph ;
                                team:statusTransitionEvent ?statusTransitionEvent ;
                                team:status ?status .
                        }
                    }
                    WHERE {
                        GRAPH ?taxonomyVersionGraph {
                            ?taxonomyGraph a team:TaxonomyGraph ;
                            team:statusTransitionEvent ?statusTransitionEvent ;
                            team:status ?status .
                        }
                    }
                    """);
            delete.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
            delete.setBinding("taxonomyGraph", targetTaxonomyGraph);

            Update insert = SPARQLFactory.updateWithConnection(
                    connection,
                    """
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    INSERT {
                        GRAPH ?taxonomyVersionGraph {
                            ?taxonomyGraph a team:TaxonomyGraph ;
                                team:statusTransitionErrorMessage ?updatedStatusTransitionErrorMessage ;
                                team:status ?updatedStatus .
                        }
                    }
                    WHERE {
                    }
                    """);
            insert.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
            insert.setBinding("taxonomyGraph", targetTaxonomyGraph);
            insert.setBinding("updatedStatusTransitionErrorMessage", vf.createLiteral(th.getMessage()));
            insert.setBinding("updatedStatus", TEAM.DRAFT);

            connection.begin();

            delete.execute();
            insert.execute();
            
            connection.commit();
        }
    }

    protected abstract Literal getTargetTaxonomyGraphStatus();

    protected abstract IRI getEventType();

    protected abstract void processEvent(RepositoryConnection connection, Model eventModel, 
            IRI event, Literal clientKey, Literal accountId, 
            IRI taxonomyVersionGraph, IRI targetTaxonomyGraph, IRI previousTaxonomyGraph, IRI contentGraph);

}
