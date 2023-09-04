package com.dalstonsemantics.confluence.semantics.cloud.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.dalstonsemantics.confluence.semantics.cloud.TestAtlassianConnectContextArgumentResolverConfigurer;
import com.dalstonsemantics.confluence.semantics.cloud.TestProviders;
import com.dalstonsemantics.confluence.semantics.cloud.TestRepositories;
import com.dalstonsemantics.confluence.semantics.cloud.TestServices;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {TestAtlassianConnectContextArgumentResolverConfigurer.class, TestProviders.class, TestRepositories.class, TestServices.class})
@TestMethodOrder(OrderAnnotation.class)
public class ImportFileEventProcessorTest {

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
    private ImportFileEventProcessor processor;

    @BeforeEach
    public void shouldCalculateTaxonomyVersionDifference() throws IOException {

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();
        ValueFactory vf = connection.getValueFactory();

        connection.clear();

        connection.begin();

        FileSystemResource versionZero = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(versionZero.getInputStream(), RDFFormat.TURTLE, vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38"));

        FileSystemResource taxonomyVersion = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-version-importing-file-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(taxonomyVersion.getInputStream(), RDFFormat.TURTLE, vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy-version/927294f7-0a9f-3d01-8120-b3ca3a45df38"));

        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/content-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38"));

        connection.commit();
    }

    @Test
    @Order(10)
    public void shouldProcessImportFileEvent() throws Exception {

        FileSystemResource event = new FileSystemResource("./src/test/resources/events/import-file.ttl");
        Model eventModel = Rio.parse(event.getInputStream(), RDFFormat.TURTLE);

        processor.onEvent(eventModel);

        ResponseEntity<String> taxonomyVersion = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/version", port), String.class);

        assertEquals(200, taxonomyVersion.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/processor/ImportFile.txt")), taxonomyVersion.getBody());   
    }

    @Test
    @Order(20)
    public void shouldProcessImportFileEventWithError() throws Exception {

        FileSystemResource event = new FileSystemResource("./src/test/resources/events/import-file.ttl");
        Model eventModel = Rio.parse(event.getInputStream(), RDFFormat.TURTLE);

        processor.onError(eventModel, new Exception("Could not parse the file"));

        ResponseEntity<String> taxonomyVersion = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/version", port), String.class);

        assertEquals(200, taxonomyVersion.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/processor/ImportFileError.txt")), taxonomyVersion.getBody());   
    }

}
