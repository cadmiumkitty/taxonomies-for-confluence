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
public class CalculateTaxonomyVersionDifferenceEventProcessor extends AbstractTaxonomyEventProcessor {

    public CalculateTaxonomyVersionDifferenceEventProcessor(@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool) {
        super(taxonomyRepositoryPool);
    }

    @Override
    protected IRI getEventType() {
        return TEAM.CALCULATE_TAXONOMY_VERSION_DIFFERENCE_EVENT;
    }

    @Override
    protected Literal getTargetTaxonomyGraphStatus() {
        return TEAM.CALCULATING_TAXONOMY_VERSION_DIFFERENCE;
    }

    @Override
    protected void processEvent(RepositoryConnection connection, Model eventModel, 
            IRI event, Literal clientKey, Literal accountId, 
            IRI taxonomyVersionGraph, IRI targetTaxonomyGraph, IRI previousTaxonomyGraph, IRI contentGraph) {

        // ******************************************************************************
        //
        // Concepts
        //
        // ******************************************************************************

        // ******************************************************************************
        //
        // Inserted Concepts
        //
        // ******************************************************************************

        Update deleteInsertedConcepts = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                DELETE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:insertedConcept ?concept .
                    }
                } WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:insertedConcept ?concept .
                    }
                }
                """);
        deleteInsertedConcepts.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deleteInsertedConcepts.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        Update insertInsertedConcepts = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:insertedConcept ?concept .
                    }
                }
                WHERE {
                    GRAPH ?targetTaxonomyGraph {
                        ?concept a skos:Concept .
                    }
                    MINUS {
                        GRAPH ?sourceTaxonomyGraph {
                            ?concept a skos:Concept .
                        }
                    }
                }
                """);
        insertInsertedConcepts.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insertInsertedConcepts.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insertInsertedConcepts.setBinding("sourceTaxonomyGraph", previousTaxonomyGraph);

        TupleQuery countInsertedConcepts = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?concept) AS ?count)
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:insertedConcept ?concept .
                    }
                }
                """);
        countInsertedConcepts.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        countInsertedConcepts.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        // ******************************************************************************
        //
        // Updated Concepts
        //
        // ******************************************************************************

        Update deleteUpdatedConcepts = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                DELETE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedConcept ?concept .
                    }
                } WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedConcept ?concept .
                    }
                }
                """);
        deleteUpdatedConcepts.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deleteUpdatedConcepts.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        Update insertUpdatedConcepts = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedConcept ?concept .
                    }
                }
                WHERE {
                    GRAPH ?targetTaxonomyGraph {
                        {
                            ?concept a skos:Concept .
                            MINUS {
                                GRAPH ?taxonomyVersionGraph {
                                    ?targetTaxonomyGraph team:insertedConcept ?concept .
                                }
                            }
                        }
                        OPTIONAL { ?concept skos:prefLabel ?prefLabel }
                        OPTIONAL { ?concept skos:altLabel ?altLabel }
                        OPTIONAL { ?concept skos:notation ?notation }
                    }
                    MINUS {
                        GRAPH ?sourceTaxonomyGraph {
                            ?concept a skos:Concept .
                            OPTIONAL { ?concept skos:prefLabel ?prefLabel }
                            OPTIONAL { ?concept skos:altLabel ?altLabel }
                            OPTIONAL { ?concept skos:notation ?notation }
                        }
                    }
                }
                """);
        insertUpdatedConcepts.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insertUpdatedConcepts.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insertUpdatedConcepts.setBinding("sourceTaxonomyGraph", previousTaxonomyGraph);

        TupleQuery countUpdatedConcepts = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?concept) AS ?count)
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedConcept ?concept .
                    }
                }
                """);
        countUpdatedConcepts.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        countUpdatedConcepts.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        // ******************************************************************************
        //
        // Deleted Concepts
        //
        // ******************************************************************************

        Update deleteDeletedConcepts = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                DELETE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedConcept ?concept .
                    }
                } WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedConcept ?concept .
                    }
                }
                """);
        deleteDeletedConcepts.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deleteDeletedConcepts.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        Update insertDeletedConcepts = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedConcept ?concept .
                    }
                }
                WHERE {
                    GRAPH ?sourceTaxonomyGraph {
                        ?concept a skos:Concept .
                    }
                    MINUS {
                        GRAPH ?targetTaxonomyGraph {
                            ?concept a skos:Concept .
                        }
                    }
                }
                """);
        insertDeletedConcepts.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insertDeletedConcepts.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insertDeletedConcepts.setBinding("sourceTaxonomyGraph", previousTaxonomyGraph);

        TupleQuery countDeletedConcepts = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?concept) AS ?count)
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedConcept ?concept .
                    }
                }
                """);
        countDeletedConcepts.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        countDeletedConcepts.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);


        // ******************************************************************************
        //
        // Classes
        //
        // ******************************************************************************

        // ******************************************************************************
        //
        // Inserted Classes
        //
        // ******************************************************************************

        Update deleteInsertedClasses = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                DELETE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:insertedClass ?class .
                    }
                } WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:insertedClass ?class .
                    }
                }
                """);
        deleteInsertedClasses.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deleteInsertedClasses.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        Update insertInsertedClasses = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:insertedClass ?class .
                    }
                }
                WHERE {
                    GRAPH ?targetTaxonomyGraph {
                        ?class a rdfs:Class .
                    }
                    MINUS {
                        GRAPH ?sourceTaxonomyGraph {
                            ?class a rdfs:Class .
                        }
                    }
                }
                """);
        insertInsertedClasses.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insertInsertedClasses.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insertInsertedClasses.setBinding("sourceTaxonomyGraph", previousTaxonomyGraph);

        TupleQuery countInsertedClasses = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?class) AS ?count)
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:insertedClass ?class .
                    }
                }
                """);
        countInsertedClasses.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        countInsertedClasses.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        // ******************************************************************************
        //
        // Updated Classes
        //
        // ******************************************************************************

        Update deleteUpdatedClasses = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                DELETE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedClass ?class .
                    }
                } WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedClass ?class .
                    }
                }
                """);
        deleteUpdatedClasses.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deleteUpdatedClasses.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        Update insertUpdatedClasses = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedClass ?class .
                    }
                }
                WHERE {
                    GRAPH ?targetTaxonomyGraph {
                        {
                            ?class a rdfs:Class .
                            MINUS {
                                GRAPH ?taxonomyVersionGraph {
                                    ?targetTaxonomyGraph team:insertedClass ?class .
                                }
                            }
                        }
                        OPTIONAL { ?class rdfs:label ?label }
                    }
                    MINUS {
                        GRAPH ?sourceTaxonomyGraph {
                            ?class a rdfs:Class .
                            OPTIONAL { ?class rdfs:label ?label }
                        }
                    }
                }
                """);
        insertUpdatedClasses.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insertUpdatedClasses.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insertUpdatedClasses.setBinding("sourceTaxonomyGraph", previousTaxonomyGraph);

        TupleQuery countUpdatedClasses = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?class) AS ?count)
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedClass ?class .
                    }
                }
                """);
        countUpdatedClasses.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        countUpdatedClasses.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        // ******************************************************************************
        //
        // Deleted Classes
        //
        // ******************************************************************************

        Update deleteDeletedClasses = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                DELETE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedClass ?class .
                    }
                } WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedClass ?class .
                    }
                }
                """);
        deleteDeletedClasses.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deleteDeletedClasses.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        Update insertDeletedClasses = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedClass ?class .
                    }
                }
                WHERE {
                    GRAPH ?sourceTaxonomyGraph {
                        ?class a rdfs:Class .
                    }
                    MINUS {
                        GRAPH ?targetTaxonomyGraph {
                            ?class a rdfs:Class .
                        }
                    }
                }
                """);
        insertDeletedClasses.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insertDeletedClasses.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insertDeletedClasses.setBinding("sourceTaxonomyGraph", previousTaxonomyGraph);

        TupleQuery countDeletedClasses = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?class) AS ?count)
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedClass ?class .
                    }
                }
                """);
        countDeletedClasses.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        countDeletedClasses.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);


        // ******************************************************************************
        //
        // Properties
        //
        // ******************************************************************************

        // ******************************************************************************
        //
        // Inserted Properties
        //
        // ******************************************************************************

        Update deleteInsertedProperties = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                DELETE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:insertedProperty ?property .
                    }
                } WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:insertedProperty ?property .
                    }
                }
                """);
        deleteInsertedProperties.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deleteInsertedProperties.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        Update insertInsertedProperties = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:insertedProperty ?property .
                    }
                }
                WHERE {
                    GRAPH ?targetTaxonomyGraph {
                        ?property a rdf:Property .
                    }
                    MINUS {
                        GRAPH ?sourceTaxonomyGraph {
                            ?property a rdf:Property .
                        }
                    }
                }
                """);
        insertInsertedProperties.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insertInsertedProperties.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insertInsertedProperties.setBinding("sourceTaxonomyGraph", previousTaxonomyGraph);

        TupleQuery countInsertedProperties = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?property) AS ?count)
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:insertedProperty ?property .
                    }
                }
                """);
        countInsertedProperties.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        countInsertedProperties.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        // ******************************************************************************
        //
        // Updated Properties
        //
        // ******************************************************************************

        Update deleteUpdatedProperties = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                DELETE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedProperty ?property .
                    }
                } WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedProperty ?property .
                    }
                }
                """);
        deleteUpdatedProperties.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deleteUpdatedProperties.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        Update insertUpdatedProperties = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedProperty ?property .
                    }
                }
                WHERE {
                    GRAPH ?targetTaxonomyGraph {
                        {
                            ?property a rdf:Property .
                            MINUS {
                                GRAPH ?taxonomyVersionGraph {
                                    ?targetTaxonomyGraph team:insertedProperty ?property .
                                }
                            }
                        }
                        OPTIONAL { ?property rdfs:label ?label }
                    }
                    MINUS {
                        GRAPH ?sourceTaxonomyGraph {
                            ?property a rdf:Property .
                            OPTIONAL { ?property rdfs:label ?label }
                        }
                    }
                }
                """);
        insertUpdatedProperties.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insertUpdatedProperties.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insertUpdatedProperties.setBinding("sourceTaxonomyGraph", previousTaxonomyGraph);

        TupleQuery countUpdatedProperties = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?property) AS ?count)
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedProperty ?property .
                    }
                }
                """);
        countUpdatedProperties.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        countUpdatedProperties.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        // ******************************************************************************
        //
        // Deleted Properties
        //
        // ******************************************************************************

        Update deleteDeletedProperties = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                DELETE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedProperty ?property .
                    }
                } WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedProperty ?property .
                    }
                }
                """);
        deleteDeletedProperties.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deleteDeletedProperties.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

        Update insertDeletedProperties = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                INSERT {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedProperty ?property .
                    }
                }
                WHERE {
                    GRAPH ?sourceTaxonomyGraph {
                        ?property a rdf:Property .
                    }
                    MINUS {
                        GRAPH ?targetTaxonomyGraph {
                            ?property a rdf:Property .
                        }
                    }
                }
                """);
        insertDeletedProperties.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        insertDeletedProperties.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        insertDeletedProperties.setBinding("sourceTaxonomyGraph", previousTaxonomyGraph);

        TupleQuery countDeletedProperties = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                SELECT (COUNT(DISTINCT ?property) AS ?count)
                WHERE {
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedProperty ?property .
                    }
                }
                """);
        countDeletedProperties.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        countDeletedProperties.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);


        connection.begin();

        deleteInsertedConcepts.execute();
        insertInsertedConcepts.execute();
        deleteUpdatedConcepts.execute();
        insertUpdatedConcepts.execute();
        deleteDeletedConcepts.execute();
        insertDeletedConcepts.execute();

        deleteInsertedClasses.execute();
        insertInsertedClasses.execute();
        deleteUpdatedClasses.execute();
        insertUpdatedClasses.execute();
        deleteDeletedClasses.execute();
        insertDeletedClasses.execute();

        deleteInsertedProperties.execute();
        insertInsertedProperties.execute();
        deleteUpdatedProperties.execute();
        insertUpdatedProperties.execute();
        deleteDeletedProperties.execute();
        insertDeletedProperties.execute();

        try (TupleQueryResult countInsertedConceptsResult = countInsertedConcepts.evaluate();
                TupleQueryResult countUpdatedConceptsResult = countUpdatedConcepts.evaluate();
                TupleQueryResult countDeletedConceptsResult = countDeletedConcepts.evaluate();
                TupleQueryResult countInsertedClassesResult = countInsertedClasses.evaluate();
                TupleQueryResult countUpdatedClassesResult = countUpdatedClasses.evaluate();
                TupleQueryResult countDeletedClassesResult = countDeletedClasses.evaluate();                                
                TupleQueryResult countInsertedPropertiesResult = countInsertedProperties.evaluate();
                TupleQueryResult countUpdatedPropertiesResult = countUpdatedProperties.evaluate();
                TupleQueryResult countDeletedPropertiesResult = countDeletedProperties.evaluate()) {

            if (countInsertedConceptsResult.hasNext() && 
                    countUpdatedConceptsResult.hasNext() && 
                    countDeletedConceptsResult.hasNext() &&                    
                    countInsertedClassesResult.hasNext() && 
                    countUpdatedClassesResult.hasNext() && 
                    countDeletedClassesResult.hasNext() &&                                        
                    countInsertedPropertiesResult.hasNext() && 
                    countUpdatedPropertiesResult.hasNext() && 
                    countDeletedPropertiesResult.hasNext()) {

                Literal insertedConceptCount = (Literal)countInsertedConceptsResult.next().getValue("count");
                Literal updatedConceptCount = (Literal)countUpdatedConceptsResult.next().getValue("count");
                Literal deletedConceptCount = (Literal)countDeletedConceptsResult.next().getValue("count");

                Literal insertedClassCount = (Literal)countInsertedClassesResult.next().getValue("count");
                Literal updatedClassCount = (Literal)countUpdatedClassesResult.next().getValue("count");
                Literal deletedClassCount = (Literal)countDeletedClassesResult.next().getValue("count");

                Literal insertedPropertyCount = (Literal)countInsertedPropertiesResult.next().getValue("count");
                Literal updatedPropertyCount = (Literal)countUpdatedPropertiesResult.next().getValue("count");
                Literal deletedPropertyCount = (Literal)countDeletedPropertiesResult.next().getValue("count");

                log.info("Created: {}. Updated: {}. Deleted: {}", insertedConceptCount, updatedConceptCount, deletedConceptCount);

                Update deleteCUD = SPARQLFactory.updateWithConnection(
                        connection,
                        """
                        PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                        DELETE {
                            GRAPH ?taxonomyVersionGraph {
                                ?targetTaxonomyGraph team:status ?status .
                                ?targetTaxonomyGraph team:statusTransitionEvent ?statusTransitionEvent .
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
                deleteCUD.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
                deleteCUD.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);

                Update insertCUD = SPARQLFactory.updateWithConnection(
                        connection,
                        """
                        PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                        INSERT {
                            GRAPH ?taxonomyVersionGraph {
                                ?targetTaxonomyGraph team:insertedConceptCount ?calculatedInsertedConceptCount .
                                ?targetTaxonomyGraph team:updatedConceptCount ?calculatedUpdatedConceptCount .
                                ?targetTaxonomyGraph team:deletedConceptCount ?calculatedDeletedConceptCount .
                                ?targetTaxonomyGraph team:insertedClassCount ?calculatedInsertedClassCount .
                                ?targetTaxonomyGraph team:updatedClassCount ?calculatedUpdatedClassCount .
                                ?targetTaxonomyGraph team:deletedClassCount ?calculatedDeletedClassCount .
                                ?targetTaxonomyGraph team:insertedPropertyCount ?calculatedInsertedPropertyCount .
                                ?targetTaxonomyGraph team:updatedPropertyCount ?calculatedUpdatedPropertyCount .
                                ?targetTaxonomyGraph team:deletedPropertyCount ?calculatedDeletedPropertyCount .
                                ?targetTaxonomyGraph team:status ?updatedStatus .
                            }
                        }
                        WHERE {
                        }
                        """);
                insertCUD.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
                insertCUD.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
                insertCUD.setBinding("calculatedInsertedConceptCount", insertedConceptCount);
                insertCUD.setBinding("calculatedUpdatedConceptCount", updatedConceptCount);
                insertCUD.setBinding("calculatedDeletedConceptCount", deletedConceptCount);

                insertCUD.setBinding("calculatedInsertedClassCount", insertedClassCount);
                insertCUD.setBinding("calculatedUpdatedClassCount", updatedClassCount);
                insertCUD.setBinding("calculatedDeletedClassCount", deletedClassCount);

                insertCUD.setBinding("calculatedInsertedPropertyCount", insertedPropertyCount);
                insertCUD.setBinding("calculatedUpdatedPropertyCount", updatedPropertyCount);
                insertCUD.setBinding("calculatedDeletedPropertyCount", deletedPropertyCount);

                insertCUD.setBinding("updatedStatus", TEAM.DRAFT);

                deleteCUD.execute();
                insertCUD.execute();

                connection.commit();
            } else {
                
                log.warn("Missing created, updated or deleted results.");
            }
        }
    }
}