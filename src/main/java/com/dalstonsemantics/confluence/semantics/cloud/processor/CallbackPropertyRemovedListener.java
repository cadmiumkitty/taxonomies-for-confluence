package com.dalstonsemantics.confluence.semantics.cloud.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;

@Component
public class CallbackPropertyRemovedListener extends AbstractEventListener {

    public CallbackPropertyRemovedListener(
            @Autowired QueueClientProvider queueClientProvider,
            @Value("${addon.queues.callback-property-removed}") String queueName,
            @Autowired CallbackPropertyRemovedProcessor callbackPropertyRemovedProcessor,
            @Value("${addon.queues.receive-visibility-timeout-sec}") long receiveVisibilityTimeoutSeconds,
            @Value("${addon.queues.receive-timeout-sec}") long receiveTimeoutSeconds) {
        super(queueClientProvider, queueName, callbackPropertyRemovedProcessor,
                receiveVisibilityTimeoutSeconds, receiveTimeoutSeconds);
    }
}
