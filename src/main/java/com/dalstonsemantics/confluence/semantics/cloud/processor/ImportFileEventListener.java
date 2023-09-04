package com.dalstonsemantics.confluence.semantics.cloud.processor;

import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImportFileEventListener extends AbstractEventListener {

    public ImportFileEventListener(
            @Autowired QueueClientProvider queueClientProvider,
            @Value("${addon.queues.import-file}") String queueName,
            @Autowired ImportFileEventProcessor importFileEventProcessor,
            @Value("${addon.queues.receive-visibility-timeout-sec}") long receiveVisibilityTimeoutSeconds,
            @Value("${addon.queues.receive-timeout-sec}") long receiveTimeoutSeconds) {
        super(queueClientProvider, queueName, importFileEventProcessor, receiveVisibilityTimeoutSeconds, receiveTimeoutSeconds);
    }
}