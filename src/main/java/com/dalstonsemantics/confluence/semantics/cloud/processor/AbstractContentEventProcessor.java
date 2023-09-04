package com.dalstonsemantics.confluence.semantics.cloud.processor;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import com.dalstonsemantics.confluence.semantics.cloud.RepositoryMaxSizeExceededException;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract event processor for all content lifecycle events (e.g. page created).
 */
@Slf4j
public abstract class AbstractContentEventProcessor implements EventProcessor {

    private Rdf4jRepositoryPool taxonomyRepositoryPool;
    protected UUIDProvider uuidp;
    private EventDispatcher dispatcher;
    private long contentMaxSize;

    public AbstractContentEventProcessor(Rdf4jRepositoryPool taxonomyRepositoryPool, UUIDProvider uuidp, EventDispatcher dispatcher, long contentMaxSize) {
        this.taxonomyRepositoryPool = taxonomyRepositoryPool;
        this.uuidp = uuidp;
        this.dispatcher = dispatcher;
        this.contentMaxSize = contentMaxSize;
    }

    public void onEvent(Model eventModel) {

        IRI event = (IRI)eventModel.getStatements(null, RDF.TYPE, getEventType()).iterator().next().getSubject();
        Literal eventClientKey = (Literal)eventModel.getStatements(event, TEAM.CLIENT_KEY, null).iterator().next().getObject();

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(eventClientKey.stringValue());

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            ValueFactory vf = taxonomyRepository.getValueFactory();
            IRI contentGraph = vf.createIRI(Namespaces.CONTENT_GRAPH, eventClientKey.stringValue());

            if (connection.size() > this.contentMaxSize) {

                log.error("Maximum size of the content repository of {} has been reached. Skipping upload.", contentMaxSize);
                throw new RepositoryMaxSizeExceededException();
            }
    
            log.info("Processing: {}", getEventType());

            processEvent(connection, eventModel, event, eventClientKey, contentGraph);

            log.info("Processing complete: {}.", getEventType());
        }
    }

    public void onError(Model eventModel, Throwable th) {
        // No compensating actions is possible
    }

    protected abstract IRI getEventType();

    protected abstract void processEvent(RepositoryConnection connection, Model eventModel, 
            IRI event, Literal clientKey, IRI contentGraph);

    protected void dispatchMaterializeContentGraphEvent(ValueFactory vf, String eventClientKey) {

        IRI event = vf.createIRI(Namespaces.EVENT, uuidp.randomUUID().toString());
            
        ModelBuilder mb = new ModelBuilder();
        mb.setNamespace(TEAM.PREFIX, TEAM.NAMESPACE);
        mb.defaultGraph()
            .subject(event)
                .add(RDF.TYPE, TEAM.MATERIALIZE_CONTENT_GRAPH_EVENT)
                .add(TEAM.CLIENT_KEY, eventClientKey); 

        Model eventModel = mb.build();

        dispatcher.dispatch(eventModel);
    }

}
