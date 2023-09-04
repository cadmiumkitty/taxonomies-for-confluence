package com.dalstonsemantics.confluence.semantics.cloud.processor;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Property;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.service.ContentService;
import com.dalstonsemantics.confluence.semantics.cloud.service.PropertyService;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CallbackPropertyCreatedUpdatedProcessor extends AbstractContentEventProcessor {

    private AtlassianHostRepository atlassianHostRepository;
    private PropertyService propertyService;
    private ContentService contentService;

    public CallbackPropertyCreatedUpdatedProcessor(
            @Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool,
            @Autowired AtlassianHostRepository atlassianHostRepository, 
            @Autowired PropertyService propertyService,
            @Autowired ContentService contentService,
            @Autowired UUIDProvider uuidp,
            @Autowired EventDispatcher dispatcher,
            @Value("${addon.repositories.taxonomy.context.content.max-size}") long contentMaxSize) {
        super(taxonomyRepositoryPool, uuidp, dispatcher, contentMaxSize);
        this.atlassianHostRepository = atlassianHostRepository;
        this.propertyService = propertyService;
        this.contentService = contentService;
    }

    @Override
    protected IRI getEventType() {
        return TEAM.CALLBACK_PROPERTY_CREATED_UPDATED_EVENT;
    }

    @SneakyThrows
    protected void processEvent(RepositoryConnection connection, Model eventModel, 
        IRI event, Literal clientKey, IRI contentGraph) {

        String eventClientKey = clientKey.stringValue();

        String eventUserAccountId = ((Literal)eventModel.getStatements(event, TEAM.ACCOUNT_ID, null).iterator().next().getObject()).stringValue();

        String eventContentId = ((Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_ID, null).iterator().next().getObject()).stringValue();
        String eventContentTitle = ((Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_TITLE, null).iterator().next().getObject()).stringValue();
        int eventContentVersion = ((Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_VERSION, null).iterator().next().getObject()).intValue();
        String eventContentContainer = ((Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_CONTAINER, null).iterator().next().getObject()).stringValue();
        Literal eventContentWhen = (Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_WHEN, null).iterator().next().getObject();
        Literal status = (Literal)eventModel.getStatements(event, TEAM.STATUS, null).iterator().next().getObject();
        
        propertyCreatedUpdated(
            connection, contentGraph,
            eventClientKey, eventUserAccountId,
            eventContentId, eventContentTitle, eventContentVersion, 
            eventContentContainer, eventContentWhen, 
            status);
    }

    public void propertyCreatedUpdated(
            RepositoryConnection connection, IRI contentGraph,
            String eventClientKey, String eventUserAccountId,
            String eventContentId, String eventContentTitle, int eventContentVersion, 
            String eventContentContainer, Literal eventContentWhen, 
            Literal status) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        AtlassianHost host = atlassianHostRepository.findById(eventClientKey).get();

        Property property = propertyService.getPropertyByPathByKey(host, eventContentContainer, eventContentTitle);

        log.info("Retrieved property from host. Host: {}. Content event: {}. Property: {}", host, eventContentId, property.getId());

        // If property is recreated, it has a new property id, so need to compare both identifiers and versions.
        if (!eventContentId.equals(property.getId()) 
                || eventContentVersion < property.getVersion().getNumber()) {
            log.info("Out of sequence event, skipping. Content event: {}. Property: {}. Event property version: {}. Content property version: {}", 
                    eventContentId, property.getId(), eventContentVersion, property.getVersion().getNumber());
            return;
        }

        Content content = contentService.getContentByPath(host, eventContentContainer);

        log.debug("Retrieved content from host. Host: {}. Content: {}", host, content);

        ValueFactory vf = connection.getValueFactory();

        // Take property statements into their own namespace
        IRI statementS = vf.createIRI(Namespaces.PROPERTY, String.format("%s-%s", eventClientKey, property.getId()));
        IRI contentS = vf.createIRI(Namespaces.CONTENT, String.format("%s-%s", eventClientKey, content.getId()));
        
        // We are not the master of property, content or agent information, Confluence stores the versions
        // and the history. We can recover missing properties, contents and agents from Confluence, not vice-versa,
        // therefore only store last activity, not full history.

        // Consider Subject and Type property as rdf:Statement about a piece of content. Use propertyVersion as
        // part of the statement to validate updates.

        // Only consider dcterms:subject and dcterms:type property of content as materialization of the Statement.
        // Content conflicts can be resolved via page events.

        TupleQuery selectProperty = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX dcterms: <http://purl.org/dc/terms/>
                PREFIX prov: <http://www.w3.org/ns/prov#>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                SELECT ?propertyId ?propertyVersion ?status
                WHERE {
                    GRAPH ?contentGraph {
                        ?statementS a rdf:Statement ;
                            team:propertyId ?propertyId ;
                            team:propertyVersion ?propertyVersion ;
                            team:status ?status .
                    }
                }
                """);
        selectProperty.setBinding("contentGraph", contentGraph);        
        selectProperty.setBinding("statementS", statementS);

        Update deleteProperty = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX dcterms: <http://purl.org/dc/terms/>
                PREFIX prov: <http://www.w3.org/ns/prov#>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                DELETE {
                    GRAPH ?contentGraph {
                        ?statementS ?statementP ?statementO .
                        ?activityS ?activityP ?activityO .
                        ?valueS ?valueP ?valueO .
                    }
                }
                WHERE {
                    GRAPH ?contentGraph {
                        ?valueS ?valueP ?valueO .
                        ?activityS ?activityP ?activityO .
                        ?statementS ?statementP ?statementO .
                        ?activityS a prov:Activity ;
                            prov:generated ?statementS ;
                            prov:used ?valueS .
                    }
                }
                """);
        deleteProperty.setBinding("contentGraph", contentGraph);
        deleteProperty.setBinding("statementS", statementS);

        IRI contentProperty = Property.SUBJECT.equals(eventContentTitle) ? DCTERMS.SUBJECT : Property.TYPE.equals(eventContentTitle) ? DCTERMS.TYPE : RDF.TYPE ;
        IRI contentPropertyValueUri = vf.createIRI(property.getValue().getUri());

        Literal propertyId = vf.createLiteral(property.getId());
        Literal propertyVersion = vf.createLiteral(property.getVersion().getNumber());
                
        Update updateProperty = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX dcterms: <http://purl.org/dc/terms/>
                PREFIX prov: <http://www.w3.org/ns/prov#>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                INSERT {
                    GRAPH ?contentGraph {
                        ?statementS a rdf:Statement ;
                            rdf:subject ?contentS ;
                            rdf:predicate ?contentProperty ;
                            rdf:object ?contentPropertyValueUri ;
                            team:propertyId ?propertyId ;
                            team:propertyVersion ?propertyVersion ;
                            team:status ?status .
                        ?agentS a prov:Agent ;
                            team:accountId ?accountId .
                        ?activityS a prov:Activity ;
                            prov:startedAtTime ?timestamp ;
                            prov:endedAtTime ?timestamp ;
                            prov:wasAssociatedWith ?agentS ;
                            prov:generated ?statementS ;
                            prov:used ?valueS .
                        ?valueS ?contentProperty ?contentPropertyValueUri .
                    }
                }
                WHERE { }
                """);
        updateProperty.setBinding("contentGraph", contentGraph);
        updateProperty.setBinding("statementS", statementS);
        updateProperty.setBinding("propertyId", propertyId);
        updateProperty.setBinding("propertyVersion", propertyVersion);
        updateProperty.setBinding("status", status);
        updateProperty.setBinding("contentS", contentS);
        updateProperty.setBinding("contentProperty", contentProperty);
        updateProperty.setBinding("contentPropertyValueUri", contentPropertyValueUri);

        IRI agentS = vf.createIRI(Namespaces.AGENT, eventUserAccountId);
        Literal accountId = vf.createLiteral(eventUserAccountId);
        
        updateProperty.setBinding("agentS", agentS);
        updateProperty.setBinding("accountId", accountId);

        IRI activityS = vf.createIRI(Namespaces.ACTIVITY, uuidp.randomUUID().toString());

        updateProperty.setBinding("activityS", activityS);
        updateProperty.setBinding("timestamp", eventContentWhen);

        BNode valueS = vf.createBNode(uuidp.randomUUID().toString());

        updateProperty.setBinding("valueS", valueS);

        connection.setIsolationLevel(IsolationLevels.READ_UNCOMMITTED);

        connection.begin();

        try (TupleQueryResult result = selectProperty.evaluate()) {
            if (result.hasNext()) {
                BindingSet bindingSet = result.next();
                int recordedPropertyVersion = Literals.getIntValue(bindingSet.getBinding("propertyVersion").getValue(), 0);
                String recordedStatus = bindingSet.getBinding("status").getValue().stringValue();
                if (!TEAM.REMOVED_STRING.equals(recordedStatus) 
                        && recordedPropertyVersion <= property.getVersion().getNumber()) {
                    deleteProperty.execute();
                    updateProperty.execute();
                } else {
                    log.info("Event out of sequence, skipping. Recorded property status: {}. Recorded property version: {}. Event version: {}", 
                            recordedStatus, recordedPropertyVersion, eventContentVersion);
                }
            } else {
                updateProperty.execute();
            }
        }

        // Update content as we may not have it recorded just yet

        TupleQuery selectContent = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX prov: <http://www.w3.org/ns/prov#>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                SELECT ?contentId ?contentVersion ?status
                WHERE {
                    GRAPH ?contentGraph {
                        ?contentS team:contentId ?contentId ;
                            team:contentVersion ?contentVersion ;
                            team:status ?status .
                    }
                }
                """);
        selectContent.setBinding("contentGraph", contentGraph);
        selectContent.setBinding("contentS", contentS);

        Update deleteContent = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX dcterms: <http://purl.org/dc/terms/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                DELETE {
                    GRAPH ?contentGraph {
                        ?contentS ?contentP ?contentO .
                    }
                }
                WHERE {
                    GRAPH ?contentGraph {
                        ?contentS ?contentP ?contentO .
                    }
                }
                """);
        deleteContent.setBinding("contentGraph", contentGraph);
        deleteContent.setBinding("contentS", contentS);
            
        IRI contentClass = vf.createIRI(TEAM.NAMESPACE, content.getType());
        Literal contentId = vf.createLiteral(content.getId());
        Literal contentVersion = vf.createLiteral(content.getVersion().getNumber());
        Literal title = vf.createLiteral(content.getTitle());
        IRI source = vf.createIRI(String.format("%s%s", content.getLinks().getBase(), content.getLinks().getWebui()));

        Update updateContent = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX dcterms: <http://purl.org/dc/terms/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                INSERT {
                    GRAPH ?contentGraph {
                        ?contentS a ?contentClass ;
                            team:contentId ?contentId ;
                            team:contentVersion ?contentVersion ;
                            team:status ?status ;
                            dcterms:title ?title ;
                            dcterms:source ?source .
                    }
                }
                WHERE { }
                """);

        updateContent.setBinding("contentGraph", contentGraph);
        updateContent.setBinding("contentS", contentS);
        updateContent.setBinding("contentClass", contentClass);
        updateContent.setBinding("contentId", contentId);
        updateContent.setBinding("contentVersion", contentVersion);
        updateContent.setBinding("status", TEAM.CURRENT);
        updateContent.setBinding("title", title);
        updateContent.setBinding("source", source);

        try (TupleQueryResult result = selectContent.evaluate()) {
            if (result.hasNext()) {
                BindingSet bindingSet = result.next();
                int recordedContentVersion = Literals.getIntValue(bindingSet.getBinding("contentVersion").getValue(), 0);
                String recordedStatus = bindingSet.getBinding("status").getValue().stringValue();
                if (!TEAM.REMOVED_STRING.equals(recordedStatus)
                        && recordedContentVersion <= content.getVersion().getNumber()) {
                    deleteContent.execute();
                    updateContent.execute();
                } else {
                    log.info("Retrieved content is stale, skipping. Recorded content status: {}. Recorded content version: {}. Retrieved content version: {}", 
                            recordedStatus, recordedContentVersion, content.getVersion().getNumber());
                }
            } else {
                updateContent.execute();
            }
        }

        dispatchMaterializeContentGraphEvent(vf, eventClientKey);

        connection.commit();
    }
}
