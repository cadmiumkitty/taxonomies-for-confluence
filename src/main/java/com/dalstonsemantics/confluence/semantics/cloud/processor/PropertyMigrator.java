package com.dalstonsemantics.confluence.semantics.cloud.processor;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Name;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Property;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Tooltip;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Version;
import com.dalstonsemantics.confluence.semantics.cloud.service.PropertyService;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * For now implement property migration as one long class completing steps one-by-one.
 * It reads URIs of impacted properties (subject, type, class), reads properties via one of the Atlassian APIs, 
 * modifies properties and writes it back to Atlassian via another API call.
 * No care is taken for retries or failures at this point (Atlassian impose limit on number of API calls).
 */
@Component
@Slf4j
public class PropertyMigrator {
    
    private AtlassianHostRepository atlassianHostRepository;
    private PropertyService propertyService;

    public PropertyMigrator(@Autowired AtlassianHostRepository atlassianHostRepository,    
            @Autowired PropertyService propertyService) {
        this.atlassianHostRepository = atlassianHostRepository;
        this.propertyService = propertyService;
    }

    public void migrateProperty(Literal clientKey, RepositoryConnection connection, 
            IRI taxonomyVersionGraph, IRI targetTaxonomyGraph, IRI contentGraph, 
            IRI content, Literal contentId) {

        boolean migratedUpdatedSubject = false;
        boolean migratedDeletedSubject = false;
        boolean migratedUpdatedType = false;
        boolean migratedDeletedType = false;
        boolean migratedUpdatedClass = false;
        boolean migratedDeletedClass = false;

        migratedUpdatedSubject = migrateUpdatedSkosProperty(clientKey, connection, 
                taxonomyVersionGraph,  targetTaxonomyGraph,  contentGraph, 
                content, DCTERMS.SUBJECT, Property.SUBJECT, contentId);  
        if (!migratedUpdatedSubject) {                                        
            migratedDeletedSubject = migrateDeletedSkosProperty(clientKey, connection, 
                taxonomyVersionGraph,  targetTaxonomyGraph,  contentGraph, 
                content, DCTERMS.SUBJECT, Property.SUBJECT, contentId);
        }

        migratedUpdatedType = migrateUpdatedSkosProperty(clientKey, connection, 
                taxonomyVersionGraph,  targetTaxonomyGraph,  contentGraph, 
                content, DCTERMS.TYPE, Property.TYPE, contentId);  
        if (!migratedUpdatedType) {                                        
            migratedDeletedType = migrateDeletedSkosProperty(clientKey, connection, 
                taxonomyVersionGraph,  targetTaxonomyGraph,  contentGraph, 
                content, DCTERMS.TYPE, Property.TYPE, contentId);
        }

        migratedUpdatedClass = migrateUpdatedRdfsProperty(clientKey, connection, 
                taxonomyVersionGraph,  targetTaxonomyGraph,  contentGraph, 
                content, RDF.TYPE, Property.CLASS, contentId);  
        if (!migratedUpdatedClass) {                                        
            migratedDeletedClass = migrateDeletedRdfsProperty(clientKey, connection, 
                taxonomyVersionGraph,  targetTaxonomyGraph,  contentGraph, 
                content, RDF.TYPE, Property.CLASS, contentId);
        }

        log.info("Migrated property. Migrated updated subject: {}. Migrated deleted subject: {}. Migrated updated type: {}. Migrated deleted type: {}. Migrated updated class: {}. Migrated deleted class: {}.", 
                migratedUpdatedSubject, migratedDeletedSubject, migratedUpdatedType, migratedDeletedType,  migratedUpdatedClass, migratedDeletedClass);
    } 

    private boolean migrateUpdatedSkosProperty(Literal clientKey, RepositoryConnection connection,
            IRI taxonomyVersionGraph, IRI targetTaxonomyGraph, IRI contentGraph, 
            IRI content, IRI predicate, String propertyKey, Literal contentId) {

        // Discover the impacts for the piece of content along with possible actions such as replacement
        TupleQuery updatedConceptQuery = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                PREFIX dcterms: <http://purl.org/dc/terms/>
                SELECT ?concept
                WHERE {
                    GRAPH ?contentGraph {
                        ?statement a rdf:Statement ;
                            rdf:subject ?content ;
                            rdf:predicate ?predicate ;
                            rdf:object ?concept ;
                            team:propertyId ?propertyId .
                    }
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedConcept ?concept .
                    }
                }
                """);
        updatedConceptQuery.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        updatedConceptQuery.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        updatedConceptQuery.setBinding("contentGraph", contentGraph);
        updatedConceptQuery.setBinding("content", content);
        updatedConceptQuery.setBinding("predicate", predicate);

        try (TupleQueryResult updatedConceptQueryResult = updatedConceptQuery.evaluate()) {

            // Set prefLabel, altLabel and notation
            if (updatedConceptQueryResult.hasNext()) {

                BindingSet bs = updatedConceptQueryResult.next();

                IRI concept = (IRI)bs.getBinding("concept").getValue();
                    
                // Same algorithm as we apply in the UI with all the current limitations of not being able to do
                // anything apart from plain "en" (including all the "en-AU", "en-GB", etc.)
                String prefLabel = connection.getStatements(concept, SKOS.PREF_LABEL, null, targetTaxonomyGraph).stream()
                    .filter(statement -> ((Literal)statement.getObject()).getLanguage().orElse("en").equals("en"))
                    .map(statement -> statement.getObject().stringValue())
                    .findFirst().orElse("");
                String notation = connection.getStatements(concept, SKOS.NOTATION, null, targetTaxonomyGraph).stream()
                    .filter(statement -> ((Literal)statement.getObject()).getLanguage().orElse("en").equals("en"))
                    .map(statement -> statement.getObject().stringValue())
                    .findFirst().orElse("");

                AtlassianHost host = atlassianHostRepository.findById(clientKey.stringValue()).get();

                Property property = propertyService.getPropertyByContentIdByKey(host, contentId.stringValue(), propertyKey);
                
                Property updatedProperty = property.toBuilder()
                    .version(Version.builder().number(property.getVersion().getNumber() + 1).build())
                    .value(property.getValue().toBuilder()
                        .notation(notation)
                        .name(Name.builder().value(prefLabel).build())
                        .tooltip(Tooltip.builder().value(prefLabel).build())
                        .build())
                    .build();

                propertyService.updateProperty(host, contentId.stringValue(), propertyKey, updatedProperty);

                log.info("Updated property. Property Key: {}. Content: {}. Content Id: {}. Updated Property: {}", propertyKey, content, contentId, updatedProperty);

                return true;
            }

            log.info("No updated property found. Property Key: {}. Content: {}. Content Id: {}.", propertyKey, content, contentId);
        }
        
        return false;
    }

    private boolean migrateDeletedSkosProperty(Literal clientKey, RepositoryConnection connection, 
            IRI taxonomyVersionGraph, IRI targetTaxonomyGraph, IRI contentGraph, 
            IRI content, IRI predicate, String propertyKey, Literal contentId) {

        // Discover the impacts for the piece of content along with possible actions such as replacement
        TupleQuery deletedConceptsQuery = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                PREFIX dcterms: <http://purl.org/dc/terms/>
                SELECT ?concept ?replacementConcept
                WHERE {
                    GRAPH ?contentGraph {
                        ?statement a rdf:Statement ;
                            rdf:subject ?content ;
                            rdf:predicate ?predicate ;
                            rdf:object ?concept ;
                            team:propertyId ?propertyId .
                    }
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedConcept ?concept .
                        OPTIONAL {
                            GRAPH ?targetTaxonomyGraph {
                                ?replacementConcept a skos:Concept ; dcterms:replaces ?concept .
                            }
                        }
                    }
                }
                """);
        deletedConceptsQuery.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deletedConceptsQuery.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        deletedConceptsQuery.setBinding("contentGraph", contentGraph);
        deletedConceptsQuery.setBinding("content", content);
        deletedConceptsQuery.setBinding("predicate", predicate);

        try (TupleQueryResult deletedConceptsQueryResult = deletedConceptsQuery.evaluate()) {

            // Set prefLabel, altLabel and notation
            if (deletedConceptsQueryResult.hasNext()) {

                BindingSet bs = deletedConceptsQueryResult.next();

                if (bs.hasBinding("replacementConcept")) {

                    IRI replacementConcept = (IRI)bs.getBinding("replacementConcept").getValue();
                        
                    // Same algorithm as we apply in the UI with all the current limitations of not being able to do
                    // anything apart from plain "en" (including all the "en-AU", "en-GB", etc.)
                    String prefLabel = connection.getStatements(replacementConcept, SKOS.PREF_LABEL, null, targetTaxonomyGraph).stream()
                        .filter(statement -> ((Literal)statement.getObject()).getLanguage().orElse("en").equals("en"))
                        .map(statement -> statement.getObject().stringValue())
                        .findFirst().orElse("");
                    String notation = connection.getStatements(replacementConcept, SKOS.NOTATION, null, targetTaxonomyGraph).stream()
                        .filter(statement -> ((Literal)statement.getObject()).getLanguage().orElse("en").equals("en"))
                        .map(statement -> statement.getObject().stringValue())
                        .findFirst().orElse("");

                    AtlassianHost host = atlassianHostRepository.findById(clientKey.stringValue()).get();

                    Property property = propertyService.getPropertyByContentIdByKey(host, contentId.stringValue(), propertyKey);
                    
                    Property updatedProperty = property.toBuilder()
                        .version(Version.builder().number(property.getVersion().getNumber() + 1).build())
                        .value(property.getValue().toBuilder()
                            .uri(replacementConcept.stringValue())
                            .notation(notation)
                            .name(Name.builder().value(prefLabel).build())
                            .tooltip(Tooltip.builder().value(prefLabel).build())
                            .build())
                        .build();
        
                    propertyService.updateProperty(host, contentId.stringValue(), propertyKey, updatedProperty);
        
                    log.info("Deleted property with replacement. Property Key: {}. Content: {}. Content Id: {}, Updated property: {}",
                            propertyKey, content, contentId, updatedProperty);
                } else {

                    AtlassianHost host = atlassianHostRepository.findById(clientKey.stringValue()).get();

                    propertyService.deleteProperty(host, contentId.stringValue(), propertyKey);

                    log.info("Deleted property. Property Key: {}. Content: {}. Content Id: {}",
                            propertyKey, content, contentId);
                }

                return true;
            }

            log.info("No deleted property found. Property Key: {}. Content: {}. Content Id: {}.", propertyKey, content, contentId);
        }
        
        return false;
    }

    private boolean migrateUpdatedRdfsProperty(Literal clientKey, RepositoryConnection connection,
            IRI taxonomyVersionGraph, IRI targetTaxonomyGraph, IRI contentGraph, 
            IRI content, IRI predicate, String propertyKey, Literal contentId) {

        // Discover the impacts for the piece of content along with possible actions such as replacement
        TupleQuery updatedClassQuery = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                PREFIX dcterms: <http://purl.org/dc/terms/>
                SELECT ?class
                WHERE {
                    GRAPH ?contentGraph {
                        ?statement a rdf:Statement ;
                            rdf:subject ?content ;
                            rdf:predicate ?predicate ;
                            rdf:object ?class ;
                            team:propertyId ?propertyId .
                    }
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedClass ?class .
                    }
                }
                """);
        updatedClassQuery.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        updatedClassQuery.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        updatedClassQuery.setBinding("contentGraph", contentGraph);
        updatedClassQuery.setBinding("content", content);
        updatedClassQuery.setBinding("predicate", predicate);

        try (TupleQueryResult updatedClassQueryResult = updatedClassQuery.evaluate()) {

            // Set prefLabel, altLabel and notation
            if (updatedClassQueryResult.hasNext()) {

                BindingSet bs = updatedClassQueryResult.next();

                IRI classIri = (IRI)bs.getBinding("class").getValue();
                    
                // Same algorithm as we apply in the UI with all the current limitations of not being able to do
                // anything apart from plain "en" (including all the "en-AU", "en-GB", etc.)
                String label = connection.getStatements(classIri, RDFS.LABEL, null, targetTaxonomyGraph).stream()
                    .filter(statement -> ((Literal)statement.getObject()).getLanguage().orElse("en").equals("en"))
                    .map(statement -> statement.getObject().stringValue())
                    .findFirst().orElse("");

                AtlassianHost host = atlassianHostRepository.findById(clientKey.stringValue()).get();

                Property property = propertyService.getPropertyByContentIdByKey(host, contentId.stringValue(), propertyKey);
                
                Property updatedProperty = property.toBuilder()
                    .version(Version.builder().number(property.getVersion().getNumber() + 1).build())
                    .value(property.getValue().toBuilder()
                        .name(Name.builder().value(label).build())
                        .tooltip(Tooltip.builder().value(label).build())
                        .build())
                    .build();

                propertyService.updateProperty(host, contentId.stringValue(), propertyKey, updatedProperty);

                log.info("Updated property. Property Key: {}. Content: {}. Content Id: {}. Updated Property: {}", propertyKey, content, contentId, updatedProperty);

                return true;
            }

            log.info("No updated property found. Property Key: {}. Content: {}. Content Id: {}.", propertyKey, content, contentId);
        }
        
        return false;
    }

    private boolean migrateDeletedRdfsProperty(Literal clientKey, RepositoryConnection connection, 
            IRI taxonomyVersionGraph, IRI targetTaxonomyGraph, IRI contentGraph, 
            IRI content, IRI predicate, String propertyKey, Literal contentId) {

        // Discover the impacts for the piece of content along with possible actions such as replacement
        TupleQuery deletedClassQuery = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                PREFIX dcterms: <http://purl.org/dc/terms/>
                SELECT ?class ?replacementClass
                WHERE {
                    GRAPH ?contentGraph {
                        ?statement a rdf:Statement ;
                            rdf:subject ?content ;
                            rdf:predicate ?predicate ;
                            rdf:object ?class ;
                            team:propertyId ?propertyId .
                    }
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedClass ?class .
                        OPTIONAL {
                            GRAPH ?targetTaxonomyGraph {
                                ?replacementClass a rdfs:Class ; dcterms:replaces ?class .
                            }
                        }
                    }
                }
                """);
        deletedClassQuery.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deletedClassQuery.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        deletedClassQuery.setBinding("contentGraph", contentGraph);
        deletedClassQuery.setBinding("content", content);
        deletedClassQuery.setBinding("predicate", predicate);

        try (TupleQueryResult deletedClassQueryResult = deletedClassQuery.evaluate()) {

            // Set label
            if (deletedClassQueryResult.hasNext()) {

                BindingSet bs = deletedClassQueryResult.next();

                if (bs.hasBinding("replacementClass")) {

                    IRI replacementClass = (IRI)bs.getBinding("replacementClass").getValue();
                        
                    // Same algorithm as we apply in the UI with all the current limitations of not being able to do
                    // anything apart from plain "en" (including all the "en-AU", "en-GB", etc.)
                    String label = connection.getStatements(replacementClass, RDFS.LABEL, null, targetTaxonomyGraph).stream()
                        .filter(statement -> ((Literal)statement.getObject()).getLanguage().orElse("en").equals("en"))
                        .map(statement -> statement.getObject().stringValue())
                        .findFirst().orElse("");

                    AtlassianHost host = atlassianHostRepository.findById(clientKey.stringValue()).get();

                    Property property = propertyService.getPropertyByContentIdByKey(host, contentId.stringValue(), propertyKey);
                    
                    Property updatedProperty = property.toBuilder()
                        .version(Version.builder().number(property.getVersion().getNumber() + 1).build())
                        .value(property.getValue().toBuilder()
                            .uri(replacementClass.stringValue())
                            .name(Name.builder().value(label).build())
                            .tooltip(Tooltip.builder().value(label).build())
                            .build())
                        .build();
        
                    propertyService.updateProperty(host, contentId.stringValue(), propertyKey, updatedProperty);
        
                    log.info("Deleted property with replacement. Property Key: {}. Content: {}. Content Id: {}, Updated property: {}",
                            propertyKey, content, contentId, updatedProperty);
                } else {

                    AtlassianHost host = atlassianHostRepository.findById(clientKey.stringValue()).get();

                    propertyService.deleteProperty(host, contentId.stringValue(), propertyKey);

                    log.info("Deleted property. Property Key: {}. Content: {}. Content Id: {}",
                            propertyKey, content, contentId);
                }

                return true;
            }

            log.info("No deleted property found. Property Key: {}. Content: {}. Content Id: {}.", propertyKey, content, contentId);
        }
        
        return false;
    }    
}
