package com.dalstonsemantics.confluence.semantics.cloud.processor;

import java.io.StringReader;
import java.time.Duration;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.QueueMessageItem;
import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/** 
 * Abstract listener to be extended to receive messages on particular queues. Use queue per message type.
 * Once message is received and converted to Rdf4j Model, it is handed off to a processor for that type of message.
 */
@Slf4j
public abstract class AbstractEventListener {

    private QueueClientProvider queueClientProvider;
    private String queueName;
    private EventProcessor processor;
    private long receiveVisibilityTimeoutSeconds;
    private long receiveTimeoutSeconds;

    public AbstractEventListener(QueueClientProvider queueClientProvider, String queueName, EventProcessor processor,
            long receiveVisibilityTimeoutSeconds, long receiveTimeoutSeconds) {
        this.queueClientProvider = queueClientProvider;
        this.queueName = queueName;
        this.processor = processor;
        this.receiveVisibilityTimeoutSeconds = receiveVisibilityTimeoutSeconds;
        this.receiveTimeoutSeconds = receiveTimeoutSeconds;
    }

    @SneakyThrows
    @Scheduled(fixedRate = 5000)
    public void receiveMessage() {

        log.debug("Checking for message on {}", queueName);

        QueueClient queueClient = queueClientProvider.getQueueClient(queueName);
        PagedIterable<QueueMessageItem> queueMessageItemPagedIterable = queueClient.receiveMessages(1, 
                Duration.ofSeconds(this.receiveVisibilityTimeoutSeconds), Duration.ofSeconds(this.receiveTimeoutSeconds), Context.NONE);
        
        for (QueueMessageItem queueMessageItem : queueMessageItemPagedIterable) {

            log.info("Received message on {}. Message Id: {}", queueName, queueMessageItem.getMessageId());

            StringReader messageTextReader = new StringReader(queueMessageItem.getBody().toString());
                        
            try {
                Model eventModel = Rio.parse(messageTextReader, RDFFormat.TURTLE);
                try {
                    processor.onEvent(eventModel);
                    log.info("Message processed. Message Id: {}", queueMessageItem.getMessageId());
                } catch (Throwable th) {
                    log.error("Something went horribly wrong processing the message. Applying compensating actions and taking message off the queue. Message Id: {}", queueMessageItem.getMessageId(), th);
                    processor.onError(eventModel, th);
                }
            } catch (Throwable th) {
                log.error("Something went horribly wrong. Taking message off the queue. Message Id: {}", queueMessageItem.getMessageId(), th);
            }
            
            queueClient.deleteMessage(queueMessageItem.getMessageId(), queueMessageItem.getPopReceipt());
        }
    }
}