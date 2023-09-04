package com.dalstonsemantics.confluence.semantics.cloud;

import static org.eclipse.rdf4j.model.util.Values.iri;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import com.dalstonsemantics.confluence.semantics.cloud.domain.webhook.EventContent;
import com.dalstonsemantics.confluence.semantics.cloud.processor.EventDispatcher;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventWebhookController {

    private static final String ACTIVE = "active";
  
    private UUIDProvider uuidp;
    private EventDispatcher dispatcher;

    private boolean enforceLicense;

    public EventWebhookController(
            UUIDProvider uuidp, 
            EventDispatcher dispatcher,
            boolean enforceLicense) {
        this.uuidp = uuidp;
        this.dispatcher = dispatcher;
        this.enforceLicense = enforceLicense;
    }

    protected void createdRemovedRestoredTrashedUpdated(String iss, String lic, String userAccountId, EventContent eventContent, Literal status) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        if (enforceLicense && !ACTIVE.equals(lic)) {
            log.warn("No active license, returning.");
            return;
        }
        
        IRI event = iri(Namespaces.EVENT, uuidp.randomUUID().toString());
            
        ModelBuilder mb = new ModelBuilder();
        mb.setNamespace(TEAM.PREFIX, TEAM.NAMESPACE);
        mb.defaultGraph()
            .subject(event)
                .add(RDF.TYPE, TEAM.CALLBACK_CONTENT_CREATED_REMOVED_RESTORED_TRASHED_UPDATED_EVENT)
                .add(TEAM.CLIENT_KEY, iss)
                .add(TEAM.ACCOUNT_ID, userAccountId)
                .add(TEAM.EVENT_CONTENT_ID, eventContent.getId())
                .add(TEAM.EVENT_CONTENT_SPACE_KEY, eventContent.getSpaceKey())
                .add(TEAM.EVENT_CONTENT_TITLE, eventContent.getTitle())
                .add(TEAM.EVENT_CONTENT_TYPE, eventContent.getContentType())
                .add(TEAM.EVENT_CONTENT_SELF, eventContent.getSelf())
                .add(TEAM.EVENT_CONTENT_VERSION, eventContent.getVersion())
                .add(TEAM.STATUS, status);            

        Model eventModel = mb.build();

        dispatcher.dispatch(eventModel);
    }
}