package com.dalstonsemantics.confluence.semantics.cloud.processor;

import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImportResourceEventListener extends AbstractEventListener {

    public ImportResourceEventListener(
            @Autowired QueueClientProvider queueClientProvider,
            @Value("${addon.queues.import-resource}") String queueName,
            @Autowired ImportResourceEventProcessor importUrlEventProcessor,
            @Value("${addon.queues.receive-visibility-timeout-sec}") long receiveVisibilityTimeoutSeconds,
            @Value("${addon.queues.receive-timeout-sec}") long receiveTimeoutSeconds) {
        super(queueClientProvider, queueName, importUrlEventProcessor, receiveVisibilityTimeoutSeconds, receiveTimeoutSeconds);
    }
}