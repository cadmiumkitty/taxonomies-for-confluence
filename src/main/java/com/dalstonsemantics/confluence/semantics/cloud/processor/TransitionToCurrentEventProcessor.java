package com.dalstonsemantics.confluence.semantics.cloud.processor;

import com.dalstonsemantics.confluence.semantics.cloud.provider.LocalDateTimeProvider;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.util.XMLGregorianCalendarUtil;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Statements;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TransitionToCurrentEventProcessor extends AbstractTaxonomyEventProcessor {

    private PropertyMigrator propertyMigrator;
    private ContentMigrator contentMigrator;
    private LocalDateTimeProvider ldtp;
    private UUIDProvider uuidp;
    private EventDispatcher dispatcher;

    public TransitionToCurrentEventProcessor(@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool,
            @Autowired PropertyMigrator propertyMigrator,
            @Autowired ContentMigrator contentMigrator,
            @Autowired LocalDateTimeProvider ldtp,
            @Autowired UUIDProvider uuidp,
            @Autowired EventDispatcher dispatcher) {
        super(taxonomyRepositoryPool);
        this.propertyMigrator = propertyMigrator;
        this.contentMigrator = contentMigrator;
        this.ldtp = ldtp;
        this.uuidp = uuidp;
        this.dispatcher = dispatcher;
    }

    @Override
    protected IRI getEventType() {
        return TEAM.TRANSITION_TO_CURRENT_EVENT;
    }

    @Override
    protected Literal getTargetTaxonomyGraphStatus() {
        return TEAM.TRANSITIONING_TO_CURRENT;
    }

    @Override
    @SneakyThrows
    protected void processEvent(RepositoryConnection connection, Model eventModel, 
            IRI event, Literal clientKey, Literal accountId, 
            IRI taxonomyVersionGraph, IRI targetTaxonomyGraph, IRI previousTaxonomyGraph, IRI contentGraph) {

        ValueFactory vf = connection.getValueFactory();

        TupleQuery impactedContent = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                PREFIX dcterms: <http://purl.org/dc/terms/>
                SELECT ?content ?contentId ?contentVersion
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:impactedContent ?content .
                        MINUS { 
                            ?targetTaxonomyGraph team:processedContent ?content .
                        }
                    }
                    GRAPH ?contentGraph {
                        ?content team:contentId ?contentId ;
                            team:contentVersion ?contentVersion .
                    }
                }
                """);
        impactedContent.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        impactedContent.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        impactedContent.setBinding("contentGraph", contentGraph);

        try (TupleQueryResult impactedContentResult = impactedContent.evaluate()) {

            impactedContentResult.stream().forEach(bs -> {

                connection.begin();

                IRI content = (IRI)bs.getBinding("content").getValue();
                Literal contentId = (Literal)bs.getBinding("contentId").getValue();
                Literal contentVersion = (Literal)bs.getBinding("contentVersion").getValue();

                Statement processingResultStatement;

                try {

                    // TODO implement appropriate retry policy to account for throuput limits, etc.

                    propertyMigrator.migrateProperty(clientKey, connection, 
                            taxonomyVersionGraph, targetTaxonomyGraph, contentGraph, 
                            content, contentId);
                    
                    contentMigrator.migrateContent(clientKey, connection, 
                            taxonomyVersionGraph, targetTaxonomyGraph, contentGraph, 
                            content, contentId, contentVersion);

                    processingResultStatement = Statements.statement(vf, targetTaxonomyGraph, TEAM.PROCESSED_CONTENT, content, taxonomyVersionGraph);
                } catch (Throwable th) {

                    log.error("Something went horribly wrong dealing with migration of property or content. Recording the error and moving on. Content: {}", content, th);

                    processingResultStatement = Statements.statement(vf, targetTaxonomyGraph, TEAM.FAILED_CONTENT, content, taxonomyVersionGraph);
                }

                connection.add(processingResultStatement, taxonomyVersionGraph);

                recordProcessedAndFailedContentCount(connection, taxonomyVersionGraph, targetTaxonomyGraph);

                connection.commit();
            });

            connection.begin();

            recordProcessedAndFailedContentCount(connection, taxonomyVersionGraph, targetTaxonomyGraph);

            connection.commit();
        }
        
        connection.begin();

        IRI nextTaxonomyGraph = vf.createIRI(Namespaces.TAXONOMY_GRAPH, "%s-%s".formatted(clientKey.stringValue(), uuidp.randomUUID().toString()));
        IRI agent = vf.createIRI(Namespaces.AGENT, accountId.stringValue());
        IRI activity = vf.createIRI(Namespaces.ACTIVITY, uuidp.randomUUID().toString());
        Literal timestamp = vf.createLiteral(XMLGregorianCalendarUtil.fromLocalDateTime(ldtp.nowInUTC()));

        IRI calculateTaxonomyVersionDifferenceEvent = vf.createIRI(Namespaces.EVENT, uuidp.randomUUID().toString());

        ModelBuilder mb = new ModelBuilder();
        mb.setNamespace(TEAM.PREFIX, TEAM.NAMESPACE);
        mb.defaultGraph()
            .subject(calculateTaxonomyVersionDifferenceEvent)
                .add(RDF.TYPE, TEAM.CALCULATE_TAXONOMY_VERSION_DIFFERENCE_EVENT)
                .add(TEAM.CLIENT_KEY, clientKey)
                .add(TEAM.ACCOUNT_ID, accountId)
                .add(TEAM.TARGET_TAXONOMY_GRAPH, nextTaxonomyGraph);

        Model calculateTaxonomyVersionDifferenceEventModel = mb.build();

        Update deleteStatus = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX prov: <http://www.w3.org/ns/prov#>
                DELETE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:status ?targetTaxonomyGraphStatus .
                        ?targetTaxonomyGraph team:statusTransitionEvent ?statusTransitionEvent .
                        ?previousTaxonomyGraph team:status ?previousTaxonomyGraphStatus . 
                    }
                }
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:status ?targetTaxonomyGraphStatus .
                        ?targetTaxonomyGraph team:statusTransitionEvent ?statusTransitionEvent .
                        ?previousTaxonomyGraph team:status ?previousTaxonomyGraphStatus .
                    }
                }
                """);
        deleteStatus.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deleteStatus.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        deleteStatus.setBinding("previousTaxonomyGraph", previousTaxonomyGraph);

        Update insertStatus = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX prov: <http://www.w3.org/ns/prov#>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:status ?updatedTargetTaxonomyGraphStatus .
                        ?previousTaxonomyGraph team:status ?updatedPreviousTaxonomyGraphStatus .
                        ?nextTaxonomyGraph a team:TaxonomyGraph ;
                            team:previousTaxonomyGraph ?targetTaxonomyGraph ;
                            team:status ?nextTaxonomyGraphStatus ;
                            team:statusTransitionEvent ?calculateTaxonomyVersionDifferenceEvent ;
                            team:conceptSchemeCount 0 ;
                            team:conceptCount 0 ;
                            team:classCount 0 ;
                            team:propertyCount 0 .
                        ?agent a prov:Agent ;
                            team:accountId ?accountId .
                        ?activity a prov:Activity ;
                            prov:startedAtTime ?timestamp ;
                            prov:endedAtTime ?timestamp ;
                            prov:wasAssociatedWith ?agent ;
                            prov:generated ?nextTaxonomyGraph .
                    }
                }
                WHERE {
                }
                """);
        insertStatus.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insertStatus.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insertStatus.setBinding("previousTaxonomyGraph", previousTaxonomyGraph);
        insertStatus.setBinding("nextTaxonomyGraph", nextTaxonomyGraph);
        insertStatus.setBinding("updatedTargetTaxonomyGraphStatus", TEAM.CURRENT);
        insertStatus.setBinding("updatedPreviousTaxonomyGraphStatus", TEAM.HISTORICAL);
        insertStatus.setBinding("nextTaxonomyGraphStatus", TEAM.CALCULATING_TAXONOMY_VERSION_DIFFERENCE);
        insertStatus.setBinding("calculateTaxonomyVersionDifferenceEvent", calculateTaxonomyVersionDifferenceEvent);
        insertStatus.setBinding("activity", activity);
        insertStatus.setBinding("agent", agent);
        insertStatus.setBinding("accountId", accountId);
        insertStatus.setBinding("timestamp", timestamp);

        deleteStatus.execute();
        insertStatus.execute();

        dispatcher.dispatch(calculateTaxonomyVersionDifferenceEventModel);

        connection.commit();
    }

    private void recordProcessedAndFailedContentCount(RepositoryConnection connection, IRI taxonomyVersionGraph, IRI targetTaxonomyGraph) {

        TupleQuery countProcessedContent = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT ?processedContentCount ?failedContentCount
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        {
                            SELECT (COUNT(DISTINCT ?processedContent) AS ?processedContentCount)
                            WHERE {
                                ?targetTaxonomyGraph team:processedContent ?processedContent .
                            }
                        }
                        UNION
                        {
                            SELECT (COUNT(DISTINCT ?failedContent) AS ?failedContentCount)
                            WHERE {
                                ?targetTaxonomyGraph team:failedContent ?failedContent .
                            }
                        }
                    }
                }
                """);
        countProcessedContent.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        countProcessedContent.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        try (TupleQueryResult countProcessedContentResult = countProcessedContent.evaluate()) {

            if (countProcessedContentResult.hasNext()) {

                Literal processedContentCount = (Literal)countProcessedContentResult.next().getBinding("processedContentCount").getValue();
                Literal failedContentCount = (Literal)countProcessedContentResult.next().getBinding("failedContentCount").getValue();

                Update deleteProcessedContentCount = SPARQLFactory.updateWithConnection(
                        connection,
                        """
                        PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                        DELETE {
                            GRAPH ?taxonomyVersionGraph {
                                ?targetTaxonomyGraph team:processedContentCount ?processedContentCount .
                                ?targetTaxonomyGraph team:failedContentCount ?failedContentCount .
                            }
                        }
                        WHERE {
                            GRAPH ?taxonomyVersionGraph {
                                OPTIONAL { ?targetTaxonomyGraph team:processedContentCount ?processedContentCount }
                                OPTIONAL { ?targetTaxonomyGraph team:failedContentCount ?failedContentCount }
                            }
                        }
                        """);
                deleteProcessedContentCount.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
                deleteProcessedContentCount.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

                Update insertProcessedContentCount = SPARQLFactory.updateWithConnection(
                        connection,
                        """
                        PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                        INSERT {
                            GRAPH ?taxonomyVersionGraph {
                                ?targetTaxonomyGraph team:processedContentCount ?calculatedProcessedContentCount .
                                ?targetTaxonomyGraph team:failedContentCount ?calculatedFailedContentCount .
                            }
                        }
                        WHERE {
                        }
                        """);
                insertProcessedContentCount.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
                insertProcessedContentCount.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
                insertProcessedContentCount.setBinding("calculatedProcessedContentCount", processedContentCount);
                insertProcessedContentCount.setBinding("calculatedFailedContentCount", failedContentCount);

                deleteProcessedContentCount.execute();
                insertProcessedContentCount.execute();
            }                       
        }
    }

}