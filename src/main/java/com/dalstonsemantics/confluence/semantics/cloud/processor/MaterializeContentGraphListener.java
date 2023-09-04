package com.dalstonsemantics.confluence.semantics.cloud.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;

@Component
public class MaterializeContentGraphListener extends AbstractEventListener {

    public MaterializeContentGraphListener(
            @Autowired QueueClientProvider queueClientProvider,
            @Value("${addon.queues.materialize-content-graph}") String queueName,
            @Autowired MaterializeContentGraphProcessor materializeContentGraphProcessor,
            @Value("${addon.queues.receive-visibility-timeout-sec}") long receiveVisibilityTimeoutSeconds,
            @Value("${addon.queues.receive-timeout-sec}") long receiveTimeoutSeconds) {
        super(queueClientProvider, queueName, materializeContentGraphProcessor,
                receiveVisibilityTimeoutSeconds, receiveTimeoutSeconds);
    }
}
