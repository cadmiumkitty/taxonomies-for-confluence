package com.dalstonsemantics.confluence.semantics.cloud.processor;

import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CalculateTaxonomyVersionDifferenceEventListener extends AbstractEventListener {

    public CalculateTaxonomyVersionDifferenceEventListener(
            @Autowired QueueClientProvider queueClientProvider,
            @Value("${addon.queues.calculate-taxonomy-version-difference}") String queueName,
            @Autowired CalculateTaxonomyVersionDifferenceEventProcessor calculateTaxonomyVersionDifferenceEventProcessor,
            @Value("${addon.queues.receive-visibility-timeout-sec}") long receiveVisibilityTimeoutSeconds,
            @Value("${addon.queues.receive-timeout-sec}") long receiveTimeoutSeconds) {
        super(queueClientProvider, queueName, calculateTaxonomyVersionDifferenceEventProcessor, 
                receiveVisibilityTimeoutSeconds, receiveTimeoutSeconds);
    }
}