package com.dalstonsemantics.confluence.semantics.cloud.provider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QueueClientProvider {

    private DefaultAzureCredential credential;
    private QueueServiceClient queueServiceClient;
    private ConcurrentMap<String, QueueClient> queueClients;

    public QueueClientProvider(@Value("${addon.queues.queue-service-client-endpoint-url}") String endpointUrl) {
        this.credential = new DefaultAzureCredentialBuilder().build();
        this.queueServiceClient = new QueueServiceClientBuilder()
                .endpoint(endpointUrl)
                .credential(credential)
                .buildClient();
        this.queueClients = new ConcurrentHashMap<>();
    }

    public QueueClient getQueueClient(String queueName) {
        return queueClients.computeIfAbsent(queueName, x -> {
            return queueServiceClient.getQueueClient(x);
        });
    }
}
