package com.dalstonsemantics.confluence.semantics.cloud.processor;

import org.eclipse.rdf4j.model.Model;

/**
 * Processor interface to be used with EventListener.
 * Once event is received and read into Rdf4j Model, its processing is handed off to implementations of this interface.
 */
public interface EventProcessor {
    public void onEvent(Model eventModel);
    public void onError(Model eventModel, Throwable th);
}
