package com.dalstonsemantics.confluence.semantics.cloud.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;

@Component
public class CallbackContentCreatedRemovedRestoredTrashedUpdatedListener extends AbstractEventListener {

    public CallbackContentCreatedRemovedRestoredTrashedUpdatedListener(
            @Autowired QueueClientProvider queueClientProvider,
            @Value("${addon.queues.callback-content-created-removed-updated}") String queueName,
            @Autowired CallbackContentCreatedRemovedRestoredTrashedUpdatedProcessor callbackContentCreatedRemovedRestoredTrashedUpdatedProcessor,
            @Value("${addon.queues.receive-visibility-timeout-sec}") long receiveVisibilityTimeoutSeconds,
            @Value("${addon.queues.receive-timeout-sec}") long receiveTimeoutSeconds) {
        super(queueClientProvider, queueName, callbackContentCreatedRemovedRestoredTrashedUpdatedProcessor,
                receiveVisibilityTimeoutSeconds, receiveTimeoutSeconds);
    }
}
