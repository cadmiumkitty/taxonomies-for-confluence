package com.dalstonsemantics.confluence.semantics.cloud.processor;

import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CalculateContentImpactEventListener extends AbstractEventListener {

    public CalculateContentImpactEventListener (
            @Autowired QueueClientProvider queueClientProvider,
            @Value("${addon.queues.calculate-content-impact}") String queueName,
            @Autowired CalculateContentImpactEventProcessor calculateContentImpactEventProcessor,
            @Value("${addon.queues.receive-visibility-timeout-sec}") long receiveVisibilityTimeoutSeconds,
            @Value("${addon.queues.receive-timeout-sec}") long receiveTimeoutSeconds) {
        super(queueClientProvider, queueName, calculateContentImpactEventProcessor, 
                receiveVisibilityTimeoutSeconds, receiveTimeoutSeconds);
    }
}