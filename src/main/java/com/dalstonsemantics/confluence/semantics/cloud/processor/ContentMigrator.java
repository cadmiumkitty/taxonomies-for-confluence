package com.dalstonsemantics.confluence.semantics.cloud.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.dalstonsemantics.confluence.semantics.cloud.domain.history.History;
import com.dalstonsemantics.confluence.semantics.cloud.service.ContentService;
import com.dalstonsemantics.confluence.semantics.cloud.service.HistoryService;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.CDataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * For now implement content migration as one long class completing steps one-by-one.
 * It reads URIs of impacted content (pages that contain concepts, classes and properties that are changing), 
 * reads content via one of the Atlassian APIs, modifies content and writes it back to Atlassian via another API call.
 * No care is taken for retries or failures at this point (Atlassian impose limit on number of API calls).
 */
@Component
@Slf4j
public class ContentMigrator {
    
    private AtlassianHostRepository atlassianHostRepository;
    private HistoryService historyService;
    private ContentService contentService;

    public ContentMigrator(@Autowired AtlassianHostRepository atlassianHostRepository,    
            @Autowired HistoryService historyService,
            @Autowired ContentService contentService) {
        this.atlassianHostRepository = atlassianHostRepository;
        this.historyService = historyService;
        this.contentService = contentService;
    }

    public void migrateContent(Literal clientKey, RepositoryConnection connection, 
            IRI taxonomyVersionGraph, IRI targetTaxonomyGraph, IRI contentGraph, 
            IRI content, Literal contentId, Literal contentVersion) {

        // ******************************************************************************
        //
        // Content impacted because of Concept changes
        //
        // ******************************************************************************

        TupleQuery updatedConcepts = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                PREFIX dcterms: <http://purl.org/dc/terms/>
                SELECT DISTINCT ?concept
                WHERE {
                    GRAPH ?contentGraph {
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?content ;
                                rdf:object ?concept .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:predicate ?concept ;
                                team:content ?content .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:object ?concept ;
                                team:content ?content .
                        }
                    }
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedConcept ?concept .
                    }
                }
                """);
        updatedConcepts.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        updatedConcepts.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        updatedConcepts.setBinding("contentGraph", contentGraph);
        updatedConcepts.setBinding("content", content);

        TupleQuery deletedConcepts = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                PREFIX dcterms: <http://purl.org/dc/terms/>
                SELECT DISTINCT ?concept ?replacementConcept
                WHERE {
                    GRAPH ?contentGraph {
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?content ;
                                rdf:object ?concept .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:predicate ?concept ;
                                team:content ?content .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:object ?concept ;
                                team:content ?content .
                        }
                    }
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedConcept ?concept .
                    }
                    OPTIONAL {
                        GRAPH ?targetTaxonomyGraph {
                            ?replacementConcept a skos:Concept ; dcterms:replaces ?concept .
                        }
                    }
                }
                """);
        deletedConcepts.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deletedConcepts.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        deletedConcepts.setBinding("contentGraph", contentGraph);
        deletedConcepts.setBinding("content", content);

        // ******************************************************************************
        //
        // Content impacted because of Class changes
        //
        // ******************************************************************************

        TupleQuery updatedClasses = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                SELECT DISTINCT ?class
                WHERE {
                    GRAPH ?contentGraph {
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?content ;
                                rdf:object ?class .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?content ;
                                rdf:predicate ?class .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:object ?class ;
                                team:content ?content .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:predicate ?class ;
                                team:content ?content .
                        }
                    }
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedClass ?class .
                    }
                }
                """);
        updatedClasses.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        updatedClasses.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        updatedClasses.setBinding("contentGraph", contentGraph);
        updatedClasses.setBinding("content", content);

        TupleQuery deletedClasses = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX dcterms: <http://purl.org/dc/terms/>
                SELECT DISTINCT ?class ?replacementClass
                WHERE {
                    GRAPH ?contentGraph {
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?content ;
                                rdf:object ?class .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?content ;
                                rdf:predicate ?class .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:object ?class ;
                                team:content ?content .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:predicate ?class ;
                                team:content ?content .
                        }
                    }
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedClass ?class .
                    }
                    OPTIONAL {
                        GRAPH ?targetTaxonomyGraph {
                            ?replacementClass a rdfs:Class ; dcterms:replaces ?class .
                        }
                    }
                }
                """);
        deletedClasses.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deletedClasses.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        deletedClasses.setBinding("contentGraph", contentGraph);
        deletedClasses.setBinding("content", content);

        // ******************************************************************************
        //
        // Content impacted because of Property changes
        //
        // ******************************************************************************

        TupleQuery updatedProperties = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                SELECT DISTINCT ?property
                WHERE {
                    GRAPH ?contentGraph {
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?content ;
                                rdf:object ?property .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?content ;
                                rdf:predicate ?property .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:object ?property ;
                                team:content ?content .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:predicate ?property ;
                                team:content ?content .
                        }
                    }
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:updatedProperty ?property .
                    }
                }
                """);
        updatedProperties.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        updatedProperties.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        updatedProperties.setBinding("contentGraph", contentGraph);
        updatedProperties.setBinding("content", content);

        TupleQuery deletedProperties = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX dcterms: <http://purl.org/dc/terms/>
                SELECT DISTINCT ?property ?replacementProperty
                WHERE {
                    GRAPH ?contentGraph {
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?content ;
                                rdf:object ?property .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:subject ?content ;
                                rdf:predicate ?property .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:object ?property ;
                                team:content ?content .
                        }
                        UNION
                        {
                            ?statement a rdf:Statement ;
                                rdf:predicate ?property ;
                                team:content ?content .
                        }
                    }
                    GRAPH ?taxonomyVersionGraph {
                        ?targetTaxonomyGraph team:deletedProperty ?property .
                    }
                    OPTIONAL {
                        GRAPH ?targetTaxonomyGraph {
                            ?replacementProperty a rdf:Property ; dcterms:replaces ?property .
                        }
                    }
                }
                """);
        deletedProperties.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
        deletedProperties.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
        deletedProperties.setBinding("contentGraph", contentGraph);
        deletedProperties.setBinding("content", content);        

        try (TupleQueryResult updatedConceptsResult = updatedConcepts.evaluate(); 
                TupleQueryResult deletedConceptsResult = deletedConcepts.evaluate();
                TupleQueryResult updatedClassesResult = updatedClasses.evaluate();
                TupleQueryResult deletedClassesResult = deletedClasses.evaluate();
                TupleQueryResult updatedPropertiesResult = updatedProperties.evaluate();
                TupleQueryResult deletedPropertiesResult = deletedProperties.evaluate()) {

            Map<String, IRI> conceptReplacements = new HashMap<>();
            Map<String, IRI> resourceReplacements = new HashMap<>();

            while (updatedConceptsResult.hasNext()) {
                BindingSet bs = updatedConceptsResult.next();
                IRI concept = (IRI)bs.getBinding("concept").getValue();
                conceptReplacements.put(concept.stringValue(), concept);
            }

            while (deletedConceptsResult.hasNext()) {
                BindingSet bs = deletedConceptsResult.next();
                IRI concept = (IRI)bs.getBinding("concept").getValue();
                Binding replacementConcept = bs.getBinding("replacementConcept");
                if (replacementConcept != null) {
                    conceptReplacements.put(concept.stringValue(), (IRI)replacementConcept.getValue());
                    resourceReplacements.put(concept.stringValue(), (IRI)replacementConcept.getValue());
                } else {
                    conceptReplacements.put(concept.stringValue(), null);
                    resourceReplacements.put(concept.stringValue(), null);
                }
            }

            while (updatedClassesResult.hasNext()) {
                BindingSet bs = updatedClassesResult.next();
                IRI c = (IRI)bs.getBinding("class").getValue();
                resourceReplacements.put(c.stringValue(), c);
            }

            while (deletedClassesResult.hasNext()) {
                BindingSet bs = deletedClassesResult.next();
                IRI c = (IRI)bs.getBinding("class").getValue();
                Binding rc = bs.getBinding("replacementClass");
                if (rc != null) {
                    resourceReplacements.put(c.stringValue(), (IRI)rc.getValue());
                } else {
                    resourceReplacements.put(c.stringValue(), null);
                }
            }

            while (updatedPropertiesResult.hasNext()) {
                BindingSet bs = updatedPropertiesResult.next();
                IRI p = (IRI)bs.getBinding("property").getValue();
                resourceReplacements.put(p.stringValue(), p);
            }

            while (deletedPropertiesResult.hasNext()) {
                BindingSet bs = deletedPropertiesResult.next();
                IRI p = (IRI)bs.getBinding("property").getValue();
                Binding rp = bs.getBinding("replacementProperty");
                if (rp != null) {
                    resourceReplacements.put(p.stringValue(), (IRI)rp.getValue());
                } else {
                    resourceReplacements.put(p.stringValue(), null);
                }
            }

            if (!conceptReplacements.isEmpty() || !resourceReplacements.isEmpty()) {

                AtlassianHost host = atlassianHostRepository.findById(clientKey.stringValue()).get();

                History history = historyService.getHistory(host, contentId.stringValue(), Literals.getIntValue(contentVersion, 0));

                Document document = Jsoup.parse(history.getContent().getBody().getStorage().getValue());

                Elements structuredMacroTaxonomiesForConfluenceRelation = document.select("ac|structured-macro[ac:name=taxonomies-for-confluence-relation]");
                for (Element element : structuredMacroTaxonomiesForConfluenceRelation) {
                    Element acParameterUri = element.getElementsByAttributeValue("ac:name", "uri").first();
                    Element acParameterUriUrl = acParameterUri.getElementsByTag("ri:url").first();
                    String acParameterUriUrlValue = acParameterUriUrl.attr("ri:value");

                    if (conceptReplacements.containsKey(acParameterUriUrlValue)) {
                        if (conceptReplacements.get(acParameterUriUrlValue) != null) {
                            updateAcStructuredMacroTaxonomiesForConfluenceRelationElement(connection, element, targetTaxonomyGraph, conceptReplacements.get(acParameterUriUrlValue));
                        } else {
                            removeAcStructuredMacroElement(element);
                        }
                    }
                }

                Elements structuredMacroTaxonomiesForConfluenceResource = document.select("ac|structured-macro[ac:name=taxonomies-for-confluence-resource]");
                for (Element element : structuredMacroTaxonomiesForConfluenceResource) {
                    Element acParameterUri = element.getElementsByAttributeValue("ac:name", "uri").first();
                    Element acParameterUriUrl = acParameterUri.getElementsByTag("ri:url").first();
                    String acParameterUriUrlValue = acParameterUriUrl.attr("ri:value");

                    if (resourceReplacements.containsKey(acParameterUriUrlValue)) {
                        if (resourceReplacements.get(acParameterUriUrlValue) != null) {
                            updateAcStructuredMacroTaxonomiesForConfluenceResourceElement(connection, element, targetTaxonomyGraph, resourceReplacements.get(acParameterUriUrlValue));
                        } else {
                            removeAcStructuredMacroElement(element);
                        }
                    }
                }

                document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
                document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
                document.outputSettings().prettyPrint(false);
                String updatedContentBodyStorage = document.body().children().toString();

                com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content updatedContent = 
                        com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content.builder()
                        .id(history.getContent().getId())
                        .type(history.getContent().getType())
                        .title(history.getContent().getTitle())
                        .version(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Version.builder()
                            .number(Literals.getIntValue(contentVersion, 0) + 1)
                            .build())
                        .body(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Body.builder()
                            .storage(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Storage.builder()
                                .value(updatedContentBodyStorage)
                                .representation("storage")
                                .build())
                            .build())
                        .build();
                
                // Update will simply fail in case of conflict of versions, which is fine.

                contentService.updateContent(host, contentId.stringValue(), updatedContent);

                log.info("Updated content. Content: {}. Content Id: {}. Updated content: {}", content, contentId, updatedContent);
            } else {

                log.info("No updates required. Content: {}. Content Id: {}.", content, contentId);
            }

        }
    }

    private void updateAcStructuredMacroTaxonomiesForConfluenceRelationElement(RepositoryConnection connection, Element element, IRI targetTaxonomyGraph, IRI concept) {

        log.info("Updating element: {}. Concept: {}", element, concept);

        connection.getStatements(concept, null, null, targetTaxonomyGraph);

        String prefLabel = connection.getStatements(concept, SKOS.PREF_LABEL, null, targetTaxonomyGraph).stream()
            .filter(statement -> ((Literal)statement.getObject()).getLanguage().orElse("en").equals("en"))
            .map(statement -> statement.getObject().stringValue())
            .findFirst().orElse("");
        List<String> altLabel = connection.getStatements(concept, SKOS.ALT_LABEL, null, targetTaxonomyGraph).stream()
            .filter(statement -> ((Literal)statement.getObject()).getLanguage().orElse("en").equals("en"))
            .map(statement -> statement.getObject().stringValue()).collect(Collectors.toList());
        List<String> notation = connection.getStatements(concept, SKOS.NOTATION, null, targetTaxonomyGraph).stream()
            .filter(statement -> ((Literal)statement.getObject()).getLanguage().orElse("en").equals("en"))
            .map(statement -> statement.getObject().stringValue()).collect(Collectors.toList());

        Element prefLabelElement = element.getElementsByAttributeValue("ac:name", "prefLabel").first();
        if (prefLabelElement != null) {
            prefLabelElement.text(prefLabel);
        }

        Element altLabelElement = element.getElementsByAttributeValue("ac:name", "altLabel").first();
        if (altLabelElement != null) {
            altLabelElement.text(String.join(",", altLabel));
        }
        
        Element notationElement = element.getElementsByAttributeValue("ac:name", "notation").first();
        if (notationElement != null) {
            notationElement.text(String.join(",", notation));
        }
        
        Element uriElement = element.getElementsByAttributeValue("ac:name", "uri").first();
        if (uriElement != null) {
            Element uriUrlElement = uriElement.getElementsByTag("ri:url").first();
            if (uriUrlElement != null) {
                uriUrlElement.attr("ri:value", concept.stringValue());
            }
        }

        Element plainTextBody = element.getElementsByTag("ac:plain-text-body").first();
        if (plainTextBody != null) {
            CDataNode cdata = new CDataNode(prefLabel);
            plainTextBody.empty();
            plainTextBody.appendChild(cdata);
        }
    }

    private void updateAcStructuredMacroTaxonomiesForConfluenceResourceElement(RepositoryConnection connection, Element element, IRI targetTaxonomyGraph, IRI resource) {

        log.info("Updating element: {}. Resource: {}", element, resource);

        connection.getStatements(resource, null, null, targetTaxonomyGraph);

        String label = connection.getStatements(resource, RDFS.LABEL, null, targetTaxonomyGraph).stream()
            .filter(statement -> ((Literal)statement.getObject()).getLanguage().orElse("en").equals("en"))
            .map(statement -> statement.getObject().stringValue())
            .findFirst().orElse("");

        Element prefLabelElement = element.getElementsByAttributeValue("ac:name", "label").first();
        if (prefLabelElement != null) {
            prefLabelElement.text(label);
        }

        Element uriElement = element.getElementsByAttributeValue("ac:name", "uri").first();
        if (uriElement != null) {
            Element uriUrlElement = uriElement.getElementsByTag("ri:url").first();
            if (uriUrlElement != null) {
                uriUrlElement.attr("ri:value", resource.stringValue());
            }
        }

        Element plainTextBody = element.getElementsByTag("ac:plain-text-body").first();
        if (plainTextBody != null) {
            CDataNode cdata = new CDataNode(label);
            plainTextBody.empty();
            plainTextBody.appendChild(cdata);
        }
    }

    private void removeAcStructuredMacroElement(Element element) {

        log.info("Replacing element with text: {}", element);

        Element plainTextBody = element.getElementsByTag("ac:plain-text-body").first();
        if (plainTextBody != null) {
            TextNode text = new TextNode(plainTextBody.text());
            element.replaceWith(text);
        }
    }
}
