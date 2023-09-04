package com.dalstonsemantics.confluence.semantics.cloud.processor;

import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CalculateContentImpactEventProcessor extends AbstractTaxonomyEventProcessor {

    public CalculateContentImpactEventProcessor(@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool) {
        super(taxonomyRepositoryPool);
    }

    @Override
    protected IRI getEventType() {
        return TEAM.CALCULATE_CONTENT_IMPACT_EVENT;
    }

    @Override
    protected Literal getTargetTaxonomyGraphStatus() {
        return TEAM.CALCULATING_CONTENT_IMPACT;
    }

    @Override
    protected void processEvent(RepositoryConnection connection, Model eventModel, 
            IRI event, Literal clientKey, Literal accountId, 
            IRI taxonomyVersionGraph, IRI targetTaxonomyGraph, IRI previousTaxonomyGraph, IRI contentGraph) {

        Update deleteImpactedContent = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                DELETE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:impactedContent ?content .
                    }
                } WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:impactedContent ?content .
                    }
                }
                """);
        deleteImpactedContent.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deleteImpactedContent.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        // ******************************************************************************
        //
        // Content impacted by Concept changes
        //
        // ******************************************************************************

        Update insertImpactedContentForConcepts = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:impactedContent ?content .
                    }
                } WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        {
                            ?targetTaxonomyGraph team:updatedConcept ?concept .
                        }
                        UNION
                        {
                            ?targetTaxonomyGraph team:deletedConcept ?concept .
                        }
                    }
                    GRAPH ?contentGraph {
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?content ;
                                rdf:object ?concept ;
                                team:status ?status .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?concept ;
                                team:content ?content ;
                                team:status ?status .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:object ?concept ;
                                team:content ?content ;
                                team:status ?status .
                        }
                        ?content team:status ?status .
                    }
                }
                """);
        insertImpactedContentForConcepts.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insertImpactedContentForConcepts.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insertImpactedContentForConcepts.setBinding("contentGraph", contentGraph);
        insertImpactedContentForConcepts.setBinding("status", TEAM.CURRENT);

        // ******************************************************************************
        //
        // Content impacted by Class changes
        //
        // ******************************************************************************

        Update insertImpactedContentForClasses = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:impactedContent ?content .
                    }
                } WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        {
                            ?targetTaxonomyGraph team:updatedClass ?class .
                        }
                        UNION
                        {
                            ?targetTaxonomyGraph team:deletedClass ?class .
                        }
                    }
                    GRAPH ?contentGraph {
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?content ;
                                rdf:object ?class ;
                                team:status ?status .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:object ?class ;
                                team:content ?content ;
                                team:status ?status .
                        }
                        ?content team:status ?status .
                    }
                }
                """);
        insertImpactedContentForClasses.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insertImpactedContentForClasses.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insertImpactedContentForClasses.setBinding("contentGraph", contentGraph);
        insertImpactedContentForClasses.setBinding("status", TEAM.CURRENT);

        // ******************************************************************************
        //
        // Content impacted by Property changes
        //
        // ******************************************************************************

        Update insertImpactedContentForProperties = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:impactedContent ?content .
                    }
                } WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        {
                            ?targetTaxonomyGraph team:updatedProperty ?property .
                        }
                        UNION
                        {
                            ?targetTaxonomyGraph team:deletedProperty ?property .
                        }
                    }
                    GRAPH ?contentGraph {
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?content ;
                                rdf:predicate ?property ;
                                team:status ?status .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:predicate ?property ;
                                team:content ?content ;
                                team:status ?status .
                        }
                        ?content team:status ?status .
                    }
                }
                """);
        insertImpactedContentForProperties.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insertImpactedContentForProperties.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insertImpactedContentForProperties.setBinding("contentGraph", contentGraph);
        insertImpactedContentForProperties.setBinding("status", TEAM.CURRENT);

        TupleQuery countImpactedContent = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?content) AS ?count)
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:impactedContent ?content .
                    }
                }
                """);
        countImpactedContent.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        countImpactedContent.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        connection.begin();

        deleteImpactedContent.execute();

        insertImpactedContentForConcepts.execute();
        insertImpactedContentForClasses.execute();
        insertImpactedContentForProperties.execute();

        try (TupleQueryResult countImpactedContentResult = countImpactedContent.evaluate()) {

            if (countImpactedContentResult.hasNext()) {

                Literal impactedCount = (Literal)countImpactedContentResult.next().getValue("count");

                log.info("Impacted: {}", impactedCount);

                Update deleteImpacted = SPARQLFactory.updateWithConnection(
                        connection,
                        """
                        PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                        DELETE {
                            GRAPH ?taxonomyVersionGraph {
                                ?targetTaxonomyGraph team:status ?status .
                                ?targetTaxonomyGraph team:statusTransitionEvent ?statusTransitionEvent .
                                ?targetTaxonomyGraph team:impactedContentCount ?impactedContentCount .
                            }
                        }
                        WHERE {
                            GRAPH ?taxonomyVersionGraph {
                                ?targetTaxonomyGraph team:status ?status .
                                ?targetTaxonomyGraph team:statusTransitionEvent ?statusTransitionEvent .
                                OPTIONAL { ?targetTaxonomyGraph team:impactedContentCount ?impactedContentCount }
                            }
                        }
                        """);
                deleteImpacted.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
                deleteImpacted.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
                
                Update insertImpacted = SPARQLFactory.updateWithConnection(
                        connection,
                        """
                        PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                        INSERT {
                            GRAPH ?taxonomyVersionGraph {
                                ?targetTaxonomyGraph team:status ?updatedStatus .
                                ?targetTaxonomyGraph team:impactedContentCount ?calculatedImpactedContentCount .
                            }
                        }
                        WHERE {
                        }
                        """);
                insertImpacted.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
                insertImpacted.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
                insertImpacted.setBinding("calculatedImpactedContentCount", impactedCount);
                insertImpacted.setBinding("updatedStatus", TEAM.AWAITING_TRANSITION_TO_CURRENT);

                deleteImpacted.execute();
                insertImpacted.execute();

                connection.commit();
            } else {

                log.warn("Missing impacted results.");
            }
        }
    }
}