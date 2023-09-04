package com.dalstonsemantics.confluence.semantics.cloud.processor;

import java.io.InputStream;

import com.azure.storage.blob.BlobClient;
import com.dalstonsemantics.confluence.semantics.cloud.RepositoryMaxSizeExceededException;
import com.dalstonsemantics.confluence.semantics.cloud.provider.BlobClientProvider;
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
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CopyFromCurrentEventProcessor extends AbstractTaxonomyEventProcessor {

    private UUIDProvider uuidp;
    private EventDispatcher dispatcher;
    private long taxonomyMaxSize;

    public CopyFromCurrentEventProcessor(
            @Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool,
            @Autowired UUIDProvider uuidp,
            @Autowired EventDispatcher dispatcher,
            @Value("${addon.repositories.taxonomy.context.taxonomy.max-size}") long taxonomyMaxSize) {
        super(taxonomyRepositoryPool);
        this.uuidp = uuidp;
        this.dispatcher = dispatcher;
        this.taxonomyMaxSize = taxonomyMaxSize;
    }

    @Override
    protected IRI getEventType() {
        return TEAM.COPY_FROM_CURRENT_EVENT;
    }

    @Override
    protected Literal getTargetTaxonomyGraphStatus() {
        return TEAM.COPYING;
    }

    @Override
    @SneakyThrows
    protected void processEvent(RepositoryConnection connection, Model eventModel, 
            IRI event, Literal clientKey, Literal accountId, 
            IRI taxonomyVersionGraph, IRI targetTaxonomyGraph, IRI previousTaxonomyGraph, IRI contentGraph) {

        ValueFactory vf = connection.getValueFactory();

        Update copyFromPreviousToTarget = SPARQLFactory.updateWithConnection(
                connection,
                """
                INSERT {
                    GRAPH ?targetTaxonomyGraph {
                        ?s ?p ?o .
                    }
                }
                WHERE {
                    GRAPH ?previousTaxonomyGraph {
                        ?s ?p ?o .
                    }
                }
                """);
        copyFromPreviousToTarget.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        copyFromPreviousToTarget.setBinding("previousTaxonomyGraph", previousTaxonomyGraph);

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

        TupleQuery countConceptSchemes = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?conceptScheme) AS ?count)
                WHERE {
                    GRAPH ?targetTaxonomyGraph {
                        ?conceptScheme a skos:ConceptScheme .
                    }
                }
                """);
        countConceptSchemes.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        TupleQuery countConcepts = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?concept) AS ?count)
                WHERE {
                    GRAPH ?targetTaxonomyGraph {
                        ?concept a skos:Concept .
                    }
                }
                """);
        countConcepts.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        TupleQuery countClasses = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?concept) AS ?count)
                WHERE {
                    GRAPH ?targetTaxonomyGraph {
                        ?concept a rdfs:Class .
                    }
                }
                """);
        countClasses.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        TupleQuery countProperties = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?concept) AS ?count)
                WHERE {
                    GRAPH ?targetTaxonomyGraph {
                        ?concept a rdf:Property .
                    }
                }
                """);
        countProperties.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        connection.begin();

        if (connection.size() < this.taxonomyMaxSize) {
            copyFromPreviousToTarget.execute();
        } else {
            log.error("Maximum size of the taxonomy repository of {} has been reached. Skipping upload.", taxonomyMaxSize);
            throw new RepositoryMaxSizeExceededException();
        }

        try (TupleQueryResult countConceptSchemesResult = countConceptSchemes.evaluate();
                TupleQueryResult countConceptsResult = countConcepts.evaluate();
                TupleQueryResult countClassesResult = countClasses.evaluate();
                TupleQueryResult countPropertiesResult = countProperties.evaluate()) {

            Literal conceptSchemeCount = (Literal)countConceptSchemesResult.next().getValue("count");
            Literal conceptCount = (Literal)countConceptsResult.next().getValue("count");
            Literal classCount = (Literal)countClassesResult.next().getValue("count");
            Literal propertyCount = (Literal)countPropertiesResult.next().getValue("count");

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
                        ?targetTaxonomyGraph team:conceptSchemeCount ?calculatedConceptSchemeCount .
                        ?targetTaxonomyGraph team:conceptCount ?calculatedConceptCount .
                        ?targetTaxonomyGraph team:classCount ?calculatedClassCount .
                        ?targetTaxonomyGraph team:propertyCount ?calculatedPropertyCount .
                    }
                } WHERE {
                }
                """);    

            insert.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
            insert.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
            insert.setBinding("calculateTaxonomyVersionDifferenceEvent", calculateTaxonomyVersionDifferenceEvent);
            insert.setBinding("updatedStatus", TEAM.CALCULATING_TAXONOMY_VERSION_DIFFERENCE);
            insert.setBinding("calculatedConceptSchemeCount", conceptSchemeCount);
            insert.setBinding("calculatedConceptCount", conceptCount);
            insert.setBinding("calculatedClassCount", classCount);
            insert.setBinding("calculatedPropertyCount", propertyCount);

            delete.execute();
            insert.execute();

            dispatcher.dispatch(calculateTaxonomyVersionDifferenceEventModel);

            connection.commit();
        }
    }
}