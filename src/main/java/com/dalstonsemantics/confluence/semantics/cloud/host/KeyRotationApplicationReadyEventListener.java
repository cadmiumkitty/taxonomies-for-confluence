package com.dalstonsemantics.confluence.semantics.cloud.host;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * At this point we can simply check if new key is being used and re-encrypt shared secrets on startup. Going forward we may move to Azure wholesale.
 */
@Slf4j
@Component
public class KeyRotationApplicationReadyEventListener {

    private static final String HOST_REPOSITORY_ID = "host";

    private Rdf4jRepositoryPool hostRepositoryPool;
    private AtlassianHostRepository atlassianHostRepository;
    private String keyIdentifier;

    public KeyRotationApplicationReadyEventListener(
            @Autowired @Qualifier("HostRepositoryPool") Rdf4jRepositoryPool hostRepositoryPool,
            @Autowired AtlassianHostRepository atlassianHostRepository,
            @Value("${addon.host.shared-secret.key-identifier}") String keyIdentifier) {
        this.hostRepositoryPool = hostRepositoryPool;
        this.atlassianHostRepository = atlassianHostRepository;
        this.keyIdentifier = keyIdentifier;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(10)
    public void reEncryptOnApplicationReady() {

        log.info("Running re-encryption on startup.");

        Repository hostRepository = hostRepositoryPool.getRepository(HOST_REPOSITORY_ID);

        try (RepositoryConnection connection = hostRepository.getConnection()) {

            connection.setIsolationLevel(IsolationLevels.NONE);

            String qs = 
                "PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/> " +
                "SELECT * " +
                "WHERE { " +
                "  ?s a team:AtlassianHost ; " +
                "    team:clientKey ?clientKey ; " +                
                "    team:keyIdentifier ?keyIdentifier . " +
                "}";
            
            TupleQuery tq = connection.prepareTupleQuery(qs);
            tq.setIncludeInferred(false);

            try (TupleQueryResult result = tq.evaluate()) {

                // Simply iterate over AtlassianHost records and save with new key if difference is detected.
                result.stream().forEach(i -> {

                    String itemClientKey = i.getValue("clientKey").stringValue();
                    String itemKeyIdentifier = i.getValue("keyIdentifier").stringValue();

                    if (!itemKeyIdentifier.equals(this.keyIdentifier)) {
                        log.info("Rotating encryption key for clientKey: {}.", itemClientKey);
                        atlassianHostRepository.save(atlassianHostRepository.findById(itemClientKey).get());
                    }
                });
            }
        }
    }        
}
