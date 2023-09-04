package com.dalstonsemantics.confluence.semantics.cloud.processor;

import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;

@Component
public class ClearEventProcessor extends AbstractTaxonomyEventProcessor {

    private UUIDProvider uuidp;
    private EventDispatcher dispatcher;

    public ClearEventProcessor(@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool,
            @Autowired UUIDProvider uuidp,
            @Autowired EventDispatcher dispatcher) {
        super(taxonomyRepositoryPool);
        this.uuidp = uuidp;
        this.dispatcher = dispatcher;
    }

    @Override
    protected IRI getEventType() {
        return TEAM.CLEAR_EVENT;
    }

    @Override
    protected Literal getTargetTaxonomyGraphStatus() {
        return TEAM.CLEARING;
    }

    @Override
    @SneakyThrows
    protected void processEvent(RepositoryConnection connection, Model eventModel, 
            IRI event, Literal clientKey, Literal accountId, 
            IRI taxonomyVersionGraph, IRI targetTaxonomyGraph, IRI previousTaxonomyGraph, IRI contentGraph) {

        ValueFactory vf = connection.getValueFactory();
        IRI calculateTaxonomyVersionDifferenceEvent = vf.createIRI(Namespaces.EVENT, uuidp.randomUUID().toString());
        
        ModelBuilder mb = new ModelBuilder();
        mb.setNamespace(TEAM.PREFIX, TEAM.NAMESPACE);
        mb.defaultGraph()
            .subject(calculateTaxonomyVersionDifferenceEvent)
                .add(RDF.TYPE, TEAM.CALCULATE_TAXONOMY_VERSION_DIFFERENCE_EVENT)
                .add(TEAM.CLIENT_KEY, clientKey)
                .add(TEAM.ACCOUNT_ID, accountId)                
                .add(TEAM.TARGET_TAXONOMY_GRAPH, targetTaxonomyGraph);

        Model calculateTaxonomyVersionDifferenceEventModel = mb.build();

        Update delete = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                DELETE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:status ?status .
                        ?targetTaxonomyGraph team:statusTransitionEvent ?statusTransitionEvent .
                        ?targetTaxonomyGraph team:conceptSchemeCount ?conceptSchemeCount .
                        ?targetTaxonomyGraph team:conceptCount ?conceptCount .
                        ?targetTaxonomyGraph team:classCount ?classCount .
                        ?targetTaxonomyGraph team:propertyCount ?propertyCount .
                        ?targetTaxonomyGraph team:insertedConceptCount ?insertedConceptCount .
                        ?targetTaxonomyGraph team:updatedConceptCount ?updatedConceptCount .
                        ?targetTaxonomyGraph team:deletedConceptCount ?deletedConceptCount .
                        ?targetTaxonomyGraph team:insertedClassCount ?insertedClassCount .
                        ?targetTaxonomyGraph team:updatedClassCount ?updatedClassCount .
                        ?targetTaxonomyGraph team:deletedClassCount ?deletedClassCount .
                        ?targetTaxonomyGraph team:insertedPropertyCount ?insertedPropertyCount .
                        ?targetTaxonomyGraph team:updatedPropertyCount ?updatedPropertyCount .
                        ?targetTaxonomyGraph team:deletedPropertyCount ?deletedPropertyCount .
                    }
                }
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:status ?status .
                        ?targetTaxonomyGraph team:statusTransitionEvent ?statusTransitionEvent .
                        OPTIONAL { ?targetTaxonomyGraph team:conceptSchemeCount ?conceptSchemeCount }
                        OPTIONAL { ?targetTaxonomyGraph team:conceptCount ?conceptCount }
                        OPTIONAL { ?targetTaxonomyGraph team:classCount ?classCount }
                        OPTIONAL { ?targetTaxonomyGraph team:propertyCount ?propertyCount }
                        OPTIONAL { ?targetTaxonomyGraph team:insertedConceptCount ?insertedConceptCount }
                        OPTIONAL { ?targetTaxonomyGraph team:updatedConceptCount ?updatedConceptCount }
                        OPTIONAL { ?targetTaxonomyGraph team:deletedConceptCount ?deletedConceptCount }
                        OPTIONAL { ?targetTaxonomyGraph team:insertedClassCount ?insertedClassCount }
                        OPTIONAL { ?targetTaxonomyGraph team:updatedClassCount ?updatedClassCount }
                        OPTIONAL { ?targetTaxonomyGraph team:deletedClassCount ?deletedClassCount }
                        OPTIONAL { ?targetTaxonomyGraph team:insertedPropertyCount ?insertedPropertyCount }
                        OPTIONAL { ?targetTaxonomyGraph team:updatedPropertyCount ?updatedPropertyCount }
                        OPTIONAL { ?targetTaxonomyGraph team:deletedPropertyCount ?deletedPropertyCount }
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
                        ?targetTaxonomyGraph team:statusTransitionEvent ?calculateTaxonomyVersionDifferenceEvent .
                        ?targetTaxonomyGraph team:conceptSchemeCount 0 .
                        ?targetTaxonomyGraph team:conceptCount 0 .
                        ?targetTaxonomyGraph team:classCount 0 .
                        ?targetTaxonomyGraph team:propertyCount 0 .
                    }
                }
                WHERE {
                }
                """);
        insert.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insert.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insert.setBinding("calculateTaxonomyVersionDifferenceEvent", calculateTaxonomyVersionDifferenceEvent);
        insert.setBinding("updatedStatus", TEAM.CALCULATING_TAXONOMY_VERSION_DIFFERENCE);

        connection.begin();

        connection.clear(targetTaxonomyGraph);
        delete.execute();
        insert.execute();

        dispatcher.dispatch(calculateTaxonomyVersionDifferenceEventModel);

        connection.commit();
    }
}