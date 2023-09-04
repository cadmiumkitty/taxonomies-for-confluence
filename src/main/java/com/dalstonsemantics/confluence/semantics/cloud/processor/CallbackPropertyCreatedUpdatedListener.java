package com.dalstonsemantics.confluence.semantics.cloud.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;

@Component
public class CallbackPropertyCreatedUpdatedListener extends AbstractEventListener {

    public CallbackPropertyCreatedUpdatedListener(
            @Autowired QueueClientProvider queueClientProvider,
            @Value("${addon.queues.callback-property-created-updated}") String queueName,
            @Autowired CallbackPropertyCreatedUpdatedProcessor callbackPropertyCreatedUpdatedProcessor,
            @Value("${addon.queues.receive-visibility-timeout-sec}") long receiveVisibilityTimeoutSeconds,
            @Value("${addon.queues.receive-timeout-sec}") long receiveTimeoutSeconds) {
        super(queueClientProvider, queueName, callbackPropertyCreatedUpdatedProcessor,
                receiveVisibilityTimeoutSeconds, receiveTimeoutSeconds);
    }
}
