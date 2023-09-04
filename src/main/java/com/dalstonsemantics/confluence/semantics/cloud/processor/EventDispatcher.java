package com.dalstonsemantics.confluence.semantics.cloud.processor;

import java.io.StringWriter;

import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Simplest possible event dispatcher that picks the queue based on the event type and configuration.
 */
@Component
@Slf4j
public class EventDispatcher {

    private QueueClientProvider queueClientProvider;
    private String importFileEventQueueName;
    private String importResourceEventQueueName;
    private String copyFromCurrentEventQueueName;
    private String calculateTaxonomyVersionDifferenceEventQueueName;
    private String calculateContentImpactEventQueueName;
    private String transitionToCurrentEventQueueName;
    private String cancelTransitionToCurrentEventQueueName;
    private String clearEventQueueName;

    private String callbackContentCreatedRemovedUpdatedEventQueueName;
    private String callbackPropertyCreatedUpdatedEventQueueName;
    private String callbackPropertyRemovedEventQueueName;
    private String materializeContentGraphEventQueueName;

    public EventDispatcher(@Autowired QueueClientProvider queueClientProvider,
            @Value("${addon.queues.import-file}") String importFileEventQueueName,
            @Value("${addon.queues.import-resource}") String importResourceEventQueueName,
            @Value("${addon.queues.copy-from-current}") String copyFromCurrentEventQueueName,
            @Value("${addon.queues.calculate-taxonomy-version-difference}") String calculateTaxonomyVersionDifferenceEventQueueName,
            @Value("${addon.queues.calculate-content-impact}") String calculateContentImpactEventQueueName,
            @Value("${addon.queues.transition-to-current}") String transitionToCurrentEventQueueName,
            @Value("${addon.queues.cancel-transition-to-current}") String cancelTransitionToCurrentEventQueueName,
            @Value("${addon.queues.clear}") String clearEventQueueName,
            @Value("${addon.queues.callback-content-created-removed-updated}") String callbackContentCreatedRemovedUpdatedEventQueueName,
            @Value("${addon.queues.callback-property-created-updated}") String callbackPropertyCreatedUpdatedEventQueueName,
            @Value("${addon.queues.callback-property-removed}") String callbackPropertyRemovedEventQueueName,
            @Value("${addon.queues.materialize-content-graph}") String materializeContentGraphEventQueueName) {
        this.queueClientProvider = queueClientProvider;
        this.importFileEventQueueName = importFileEventQueueName;
        this.importResourceEventQueueName = importResourceEventQueueName;  
        this.copyFromCurrentEventQueueName = copyFromCurrentEventQueueName;
        this.calculateTaxonomyVersionDifferenceEventQueueName = calculateTaxonomyVersionDifferenceEventQueueName;
        this.calculateContentImpactEventQueueName = calculateContentImpactEventQueueName;
        this.transitionToCurrentEventQueueName = transitionToCurrentEventQueueName;
        this.cancelTransitionToCurrentEventQueueName = cancelTransitionToCurrentEventQueueName;
        this.clearEventQueueName = clearEventQueueName;
        this.callbackContentCreatedRemovedUpdatedEventQueueName = callbackContentCreatedRemovedUpdatedEventQueueName;
        this.callbackPropertyCreatedUpdatedEventQueueName = callbackPropertyCreatedUpdatedEventQueueName;
        this.callbackPropertyRemovedEventQueueName = callbackPropertyRemovedEventQueueName;
        this.materializeContentGraphEventQueueName = materializeContentGraphEventQueueName;
    }

    @SneakyThrows
    public void dispatch(Model eventModel) {

        StringWriter stringWriter = new StringWriter();
        Rio.write(eventModel, stringWriter, RDFFormat.TURTLE);

        IRI eventClass = (IRI)eventModel.getStatements(null, RDF.TYPE, null).iterator().next().getObject();
        if (eventClass.equals(TEAM.IMPORT_FILE_EVENT)) {
            queueClientProvider.getQueueClient(importFileEventQueueName).sendMessage(stringWriter.toString());
        } else if (eventClass.equals(TEAM.IMPORT_RESOURCE_EVENT)) {
            queueClientProvider.getQueueClient(importResourceEventQueueName).sendMessage(stringWriter.toString());
        } else if (eventClass.equals(TEAM.COPY_FROM_CURRENT_EVENT)) {
            queueClientProvider.getQueueClient(copyFromCurrentEventQueueName).sendMessage(stringWriter.toString());
        } else if (eventClass.equals(TEAM.CALCULATE_TAXONOMY_VERSION_DIFFERENCE_EVENT)) {
            queueClientProvider.getQueueClient(calculateTaxonomyVersionDifferenceEventQueueName).sendMessage(stringWriter.toString());
        } else if (eventClass.equals(TEAM.CALCULATE_CONTENT_IMPACT_EVENT)) {
            queueClientProvider.getQueueClient(calculateContentImpactEventQueueName).sendMessage(stringWriter.toString());
        } else if (eventClass.equals(TEAM.TRANSITION_TO_CURRENT_EVENT)) {
            queueClientProvider.getQueueClient(transitionToCurrentEventQueueName).sendMessage(stringWriter.toString());
        } else if (eventClass.equals(TEAM.CANCEL_TRANSITION_TO_CURRENT_EVENT)) {
            queueClientProvider.getQueueClient(cancelTransitionToCurrentEventQueueName).sendMessage(stringWriter.toString());
        } else if (eventClass.equals(TEAM.CLEAR_EVENT)) {
            queueClientProvider.getQueueClient(clearEventQueueName).sendMessage(stringWriter.toString());
        } else if (eventClass.equals(TEAM.CALLBACK_CONTENT_CREATED_REMOVED_RESTORED_TRASHED_UPDATED_EVENT)) {
            queueClientProvider.getQueueClient(callbackContentCreatedRemovedUpdatedEventQueueName).sendMessage(stringWriter.toString());
        } else if (eventClass.equals(TEAM.CALLBACK_PROPERTY_CREATED_UPDATED_EVENT)) {
            queueClientProvider.getQueueClient(callbackPropertyCreatedUpdatedEventQueueName).sendMessage(stringWriter.toString());
        } else if (eventClass.equals(TEAM.CALLBACK_PROPERTY_REMOVED_EVENT)) {
            queueClientProvider.getQueueClient(callbackPropertyRemovedEventQueueName).sendMessage(stringWriter.toString());
        } else if (eventClass.equals(TEAM.MATERIALIZE_CONTENT_GRAPH_EVENT)) {
            queueClientProvider.getQueueClient(materializeContentGraphEventQueueName).sendMessage(stringWriter.toString());
        } else {
            log.warn("Unknown event class: {}", eventClass);
        }
    }
}
