package com.dalstonsemantics.confluence.semantics.cloud;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Property;
import com.dalstonsemantics.confluence.semantics.cloud.domain.webhook.content.ContentEvent;
import com.dalstonsemantics.confluence.semantics.cloud.processor.EventDispatcher;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.resolver.IssParam;
import com.dalstonsemantics.confluence.semantics.cloud.util.XMLGregorianCalendarUtil;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ContentWebhookController {

    private static final String ACTIVE = "active";
  
    private UUIDProvider uuidp;
    private EventDispatcher dispatcher;

    private boolean enforceLicense;

    public ContentWebhookController(
            @Autowired UUIDProvider uuidp,
            @Autowired EventDispatcher dispatcher,
            @Value("${addon.license.enforce}") boolean enforceLicense) {
        this.uuidp = uuidp;
        this.dispatcher = dispatcher;
        this.enforceLicense = enforceLicense;
    }

    @PostMapping(value = "/webhook/content_created", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void contentCreated(@IssParam String iss, @RequestParam String lic, @RequestBody ContentEvent contentEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Received content_created. Iss: {}. Lic: {}. Content event: {}", iss, lic, contentEvent.getId());
        
        propertyCreatedUpdated(iss, lic, contentEvent, TEAM.CURRENT);

        log.info("Completed processing of content_created.");
    }

    @PostMapping(value = "/webhook/content_updated")
    @ResponseStatus(value = HttpStatus.OK)
    public void contentUpdated(@IssParam String iss, @RequestParam String lic, @RequestBody ContentEvent contentEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Received content_updated. Iss: {}. Lic: {}. Content event: {}", iss, lic, contentEvent.getId());

        propertyCreatedUpdated(iss, lic, contentEvent, TEAM.CURRENT);

        log.info("Completed processing of content_updated.");
    }

    @PostMapping(value = "/webhook/content_removed")
    @ResponseStatus(value = HttpStatus.OK)
    public void contentRemoved(@IssParam String iss, @RequestParam String lic, @RequestBody ContentEvent contentEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Received content_removed. Iss: {}. Lic: {}. Content event: {}", iss, lic, contentEvent.getId());

        propertyRemoved(iss, lic, contentEvent);

        log.info("Completed processing of content_removed.");
    }

    public void propertyCreatedUpdated(String iss, @RequestParam String lic, @RequestBody ContentEvent contentEvent, Literal status) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        if (enforceLicense && !ACTIVE.equals(lic)) {
            log.info("No active license, returning.");
            return;
        }

        // Only process type "com.atlassian.confluence.plugins.confluence-content-property-storage:content-property"
        // and title "taxonomies-for-confluence-subject" or "taxonomies-for-confluence-type".

        if (!(ContentEvent.CONTENT_PROPERTY.equals(contentEvent.getType()) 
                && (Property.SUBJECT.equals(contentEvent.getTitle()) || Property.TYPE.equals(contentEvent.getTitle()) || Property.CLASS.equals(contentEvent.getTitle())))) {
            log.info("Not a subject or type content property event. Returning. Type: {}. Title: {}", 
                    contentEvent.getType(), contentEvent.getTitle());
            return;
        }

        IRI event = iri(Namespaces.EVENT, uuidp.randomUUID().toString());
            
        ModelBuilder mb = new ModelBuilder();
        mb.setNamespace(TEAM.PREFIX, TEAM.NAMESPACE);
        mb.defaultGraph()
            .subject(event)
                .add(RDF.TYPE, TEAM.CALLBACK_PROPERTY_CREATED_UPDATED_EVENT)
                .add(TEAM.CLIENT_KEY, iss)
                .add(TEAM.ACCOUNT_ID, contentEvent.getVersion().getBy().getAccountId())
                .add(TEAM.EVENT_CONTENT_ID, contentEvent.getId())
                .add(TEAM.EVENT_CONTENT_TITLE, contentEvent.getTitle())
                .add(TEAM.EVENT_CONTENT_TYPE, contentEvent.getType())
                .add(TEAM.EVENT_CONTENT_CONTAINER, contentEvent.getExpandable().getContainer())
                .add(TEAM.EVENT_CONTENT_VERSION, contentEvent.getVersion().getNumber())
                .add(TEAM.EVENT_CONTENT_WHEN, literal(XMLGregorianCalendarUtil.fromCalendar(contentEvent.getVersion().getWhen())))
                .add(TEAM.STATUS, status);

        Model eventModel = mb.build();

        dispatcher.dispatch(eventModel);
    }

    public void propertyRemoved(String iss, @RequestParam String lic, @RequestBody ContentEvent contentEvent) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        if (enforceLicense && !ACTIVE.equals(lic)) {
            log.info("No active license, returning.");
            return;
        }
                
        // Only process type "com.atlassian.confluence.plugins.confluence-content-property-storage:content-property"
        // and title "taxonomies-for-confluence-subject", "taxonomies-for-confluence-type" or "taxonomies-for-confluence-class".

        if (!(ContentEvent.CONTENT_PROPERTY.equals(contentEvent.getType()) 
            && (Property.SUBJECT.equals(contentEvent.getTitle()) || Property.TYPE.equals(contentEvent.getTitle()) || Property.CLASS.equals(contentEvent.getTitle())))) {
            log.info("Not a subject or type content property event. Returning. Type: {}. Title: {}", 
                    contentEvent.getType(), contentEvent.getTitle());
            return;
        }

        IRI event = iri(Namespaces.EVENT, uuidp.randomUUID().toString());
            
        ModelBuilder mb = new ModelBuilder();
        mb.setNamespace(TEAM.PREFIX, TEAM.NAMESPACE);
        mb.defaultGraph()
            .subject(event)
                .add(RDF.TYPE, TEAM.CALLBACK_PROPERTY_REMOVED_EVENT)
                .add(TEAM.CLIENT_KEY, iss)
                .add(TEAM.ACCOUNT_ID, contentEvent.getVersion().getBy().getAccountId())
                .add(TEAM.EVENT_CONTENT_ID, contentEvent.getId())
                .add(TEAM.EVENT_CONTENT_TITLE, contentEvent.getTitle())
                .add(TEAM.EVENT_CONTENT_TYPE, contentEvent.getType())
                .add(TEAM.EVENT_CONTENT_CONTAINER, contentEvent.getExpandable().getContainer())
                .add(TEAM.EVENT_CONTENT_VERSION, contentEvent.getVersion().getNumber())
                .add(TEAM.EVENT_CONTENT_WHEN, literal(XMLGregorianCalendarUtil.fromCalendar(contentEvent.getVersion().getWhen())));

        Model eventModel = mb.build();

        dispatcher.dispatch(eventModel);
    }
}