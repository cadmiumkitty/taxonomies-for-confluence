package com.dalstonsemantics.confluence.semantics.cloud.processor;

import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TransitionToCurrentEventListener extends AbstractEventListener {

    public TransitionToCurrentEventListener(
            @Autowired QueueClientProvider queueClientProvider,
            @Value("${addon.queues.transition-to-current}") String queueName,
            @Autowired TransitionToCurrentEventProcessor transitionToCurrentEventProcessor,
            @Value("${addon.queues.receive-visibility-timeout-sec}") long receiveVisibilityTimeoutSeconds,
            @Value("${addon.queues.receive-timeout-sec}") long receiveTimeoutSeconds) {
        super(queueClientProvider, queueName, transitionToCurrentEventProcessor, receiveVisibilityTimeoutSeconds, receiveTimeoutSeconds);
    }
}