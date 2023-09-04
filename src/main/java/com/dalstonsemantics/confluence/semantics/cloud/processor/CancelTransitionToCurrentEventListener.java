package com.dalstonsemantics.confluence.semantics.cloud.processor;

import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CancelTransitionToCurrentEventListener extends AbstractEventListener {

    public CancelTransitionToCurrentEventListener(
            @Autowired QueueClientProvider queueClientProvider,
            @Value("${addon.queues.cancel-transition-to-current}") String queueName,
            @Autowired CancelTransitionToCurrentEventProcessor cancelTransitionToCurrentEventProcessor,
            @Value("${addon.queues.receive-visibility-timeout-sec}") long receiveVisibilityTimeoutSeconds,
            @Value("${addon.queues.receive-timeout-sec}") long receiveTimeoutSeconds) {
        super(queueClientProvider, queueName, cancelTransitionToCurrentEventProcessor, 
                receiveVisibilityTimeoutSeconds, receiveTimeoutSeconds);
    }
}