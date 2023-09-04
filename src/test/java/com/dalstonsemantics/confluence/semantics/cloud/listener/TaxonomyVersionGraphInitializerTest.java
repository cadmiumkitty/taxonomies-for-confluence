package com.dalstonsemantics.confluence.semantics.cloud.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import com.atlassian.connect.spring.AtlassianHost;
import com.dalstonsemantics.confluence.semantics.cloud.TestAtlassianConnectContextArgumentResolverConfigurer;
import com.dalstonsemantics.confluence.semantics.cloud.TestProviders;
import com.dalstonsemantics.confluence.semantics.cloud.TestRepositories;
import com.dalstonsemantics.confluence.semantics.cloud.TestServices;
import com.dalstonsemantics.confluence.semantics.cloud.TestServicesUtils;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {TestAtlassianConnectContextArgumentResolverConfigurer.class, TestProviders.class, TestRepositories.class, TestServices.class})
@TestMethodOrder(OrderAnnotation.class)
public class TaxonomyVersionGraphInitializerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired 
    @Qualifier("HostRepositoryPool") 
    private Rdf4jRepositoryPool hostRepositoryPool;

    @Autowired 
    @Qualifier("TaxonomyRepositoryPool") 
    private Rdf4jRepositoryPool taxonomyRepositoryPool;

    @Autowired
    private TaxonomyVersionGraphInitializer taxonomyVersionGraphInitializer;

    @Autowired
    private UUIDProvider uuidProvider;

    @BeforeAll
    public void shouldCalculateTaxonomyVersionDifference() throws IOException {

        TestServicesUtils.resetUUIDProviderSequence(uuidProvider);

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();
        connection.clear();
    }

    @Test
    @Order(10)
    public void shouldProcessOnApplicationEvent() throws Exception {

        AtlassianHost host = new AtlassianHost();
        host.setClientKey("927294f7-0a9f-3d01-8120-b3ca3a45df38");
        host.setCreatedBy("557058:d092c978-5ab7-4c8a-8d87-32ffac22c584");

        taxonomyVersionGraphInitializer.initializeTaxonomyVersionGraph(host);

        ResponseEntity<String> taxonomyVersion = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/version", port), String.class);

        assertEquals(200, taxonomyVersion.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/listener/AddonInstalled.txt")), taxonomyVersion.getBody());

    }
}
