package com.dalstonsemantics.confluence.semantics.cloud.processor;

import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ClearEventListener extends AbstractEventListener {

    public ClearEventListener(
            @Autowired QueueClientProvider queueClientProvider,
            @Value("${addon.queues.clear}") String queueName,
            @Autowired ClearEventProcessor clearEventProcessor,
            @Value("${addon.queues.receive-visibility-timeout-sec}") long receiveVisibilityTimeoutSeconds,
            @Value("${addon.queues.receive-timeout-sec}") long receiveTimeoutSeconds) {
        super(queueClientProvider, queueName, clearEventProcessor, receiveVisibilityTimeoutSeconds, receiveTimeoutSeconds);
    }
}