package com.dalstonsemantics.confluence.semantics.cloud.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.core.annotation.Order;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;

import com.dalstonsemantics.confluence.semantics.cloud.TestAtlassianConnectContextArgumentResolverConfigurer;
import com.dalstonsemantics.confluence.semantics.cloud.TestProviders;
import com.dalstonsemantics.confluence.semantics.cloud.TestRepositories;
import com.dalstonsemantics.confluence.semantics.cloud.TestServices;
import com.dalstonsemantics.confluence.semantics.cloud.TestServicesUtils;
import com.dalstonsemantics.confluence.semantics.cloud.TestUtils;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.service.ContentService;
import com.dalstonsemantics.confluence.semantics.cloud.service.HistoryService;
import com.dalstonsemantics.confluence.semantics.cloud.service.PropertyService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {TestAtlassianConnectContextArgumentResolverConfigurer.class, TestProviders.class, TestRepositories.class, TestServices.class})
@TestMethodOrder(OrderAnnotation.class)
public class CallbackPropertyRemovedTest {

    @Autowired 
    @Qualifier("TaxonomyRepositoryPool") 
    private Rdf4jRepositoryPool taxonomyRepositoryPool;

    @Autowired
    private CallbackPropertyRemovedProcessor processor;

    @Autowired
    private UUIDProvider uuidProvider;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private HistoryService historyService;

    @BeforeAll
    public void resetContentServiceMock() throws Exception {
        TestServicesUtils.resetPageContentToVersionOne(contentService, historyService);
    }

    @BeforeEach
    public void shouldCalculateTaxonomyVersionDifference() throws IOException {
        TestServicesUtils.resetUUIDProviderSequence(uuidProvider);
    }    

    @Test
    @Order(10)
    public void shouldProcessPropertyRemoved() throws Exception {    

        TestServicesUtils.resetPagePropertyToVersionTwoWithUpdatedExtractions(propertyService);

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();
        
        ValueFactory vf = connection.getValueFactory();
        IRI contentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        connection.clear();

        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/content-pre-webhook-property-removed-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, contentGraph);

        FileSystemResource event = new FileSystemResource("./src/test/resources/events/webhook-property-removed.ttl");
        Model eventModel = Rio.parse(event.getInputStream(), RDFFormat.TURTLE);

        processor.onEvent(eventModel);

        String expected = Files.readString(Paths.get("./src/test/resources/repositories/client/content-post-webhook-property-removed-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl"));
        String actual = TestUtils.getRepositoryContent(connection, contentGraph);
        assertEquals(expected, actual);
    }
}
