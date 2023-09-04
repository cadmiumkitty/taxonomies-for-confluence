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

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {TestAtlassianConnectContextArgumentResolverConfigurer.class, TestProviders.class, TestRepositories.class, TestServices.class})
@TestMethodOrder(OrderAnnotation.class)
public class CallbackContentCreatedRemovedRestoredTrashedUpdatedProcessorTest {

    @Autowired 
    @Qualifier("TaxonomyRepositoryPool") 
    private Rdf4jRepositoryPool taxonomyRepositoryPool;

    @Autowired
    private CallbackContentCreatedRemovedRestoredTrashedUpdatedProcessor processor;

    @Autowired
    private UUIDProvider uuidProvider;

    @Autowired
    private ContentService contentService;

    @Autowired
    private HistoryService historyService;

    @BeforeEach
    public void shouldCalculateTaxonomyVersionDifference() throws IOException {

        TestServicesUtils.resetUUIDProviderSequence(uuidProvider);
    }    

    @Test
    @Order(10)
    public void shouldProcessPageCreated() throws Exception {    

        TestServicesUtils.resetPageContentToVersionOne(contentService, historyService);
        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();
        
        ValueFactory vf = connection.getValueFactory();
        IRI contentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        connection.clear();

        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/content-pre-webhook-page-created-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, contentGraph);

        FileSystemResource event = new FileSystemResource("./src/test/resources/events/webhook-page-created.ttl");
        Model eventModel = Rio.parse(event.getInputStream(), RDFFormat.TURTLE);

        processor.onEvent(eventModel);

        String expected = Files.readString(Paths.get("./src/test/resources/repositories/client/content-post-webhook-page-created-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl"));
        String actual = TestUtils.getRepositoryContent(connection, contentGraph);
        assertEquals(expected, actual);
    }

    @Test
    @Order(20)
    public void shouldProcessPageUpdated() throws Exception {

        TestServicesUtils.resetPageContentToVersionTwoWithUpdatedContent(contentService, historyService);

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();
        
        ValueFactory vf = connection.getValueFactory();
        IRI contentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        connection.clear();

        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/content-pre-webhook-page-updated-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, contentGraph);

        FileSystemResource event = new FileSystemResource("./src/test/resources/events/webhook-page-updated.ttl");
        Model eventModel = Rio.parse(event.getInputStream(), RDFFormat.TURTLE);

        processor.onEvent(eventModel);

        String expected = Files.readString(Paths.get("./src/test/resources/repositories/client/content-post-webhook-page-updated-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl"));
        String actual = TestUtils.getRepositoryContent(connection, contentGraph);
        assertEquals(expected, actual);
    }

    @Test
    @Order(30)
    public void shouldProcessPageTrashed() throws Exception {

        TestServicesUtils.resetPageContentToVersionTwoWithUpdatedContent(contentService, historyService);

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        ValueFactory vf = connection.getValueFactory();
        IRI contentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        connection.clear();

        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/content-pre-webhook-page-trashed-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, contentGraph);

        FileSystemResource event = new FileSystemResource("./src/test/resources/events/webhook-page-trashed.ttl");
        Model eventModel = Rio.parse(event.getInputStream(), RDFFormat.TURTLE);

        processor.onEvent(eventModel);

        String expected = Files.readString(Paths.get("./src/test/resources/repositories/client/content-post-webhook-page-trashed-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl"));
        String actual = TestUtils.getRepositoryContent(connection, contentGraph);
        assertEquals(expected, actual);
    }

    @Test
    @Order(40)
    public void shouldProcessPageRestored() throws Exception {

        TestServicesUtils.resetPageContentToVersionTwoWithUpdatedContent(contentService, historyService);

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        ValueFactory vf = connection.getValueFactory();
        IRI contentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        connection.clear();

        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/content-pre-webhook-page-restored-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, contentGraph);

        FileSystemResource event = new FileSystemResource("./src/test/resources/events/webhook-page-restored.ttl");
        Model eventModel = Rio.parse(event.getInputStream(), RDFFormat.TURTLE);

        processor.onEvent(eventModel);

        String expected = Files.readString(Paths.get("./src/test/resources/repositories/client/content-post-webhook-page-restored-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl"));
        String actual = TestUtils.getRepositoryContent(connection, contentGraph);
        assertEquals(expected, actual);
    }

    @Test
    @Order(50)
    public void shouldProcessPageRemoved() throws Exception {

        TestServicesUtils.resetPageContentToVersionTwoWithUpdatedContent(contentService, historyService);

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        ValueFactory vf = connection.getValueFactory();
        IRI contentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        connection.clear();

        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/content-pre-webhook-page-removed-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, contentGraph);

        FileSystemResource event = new FileSystemResource("./src/test/resources/events/webhook-page-removed.ttl");
        Model eventModel = Rio.parse(event.getInputStream(), RDFFormat.TURTLE);

        processor.onEvent(eventModel);

        String expected = Files.readString(Paths.get("./src/test/resources/repositories/client/content-post-webhook-page-removed-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl"));
        String actual = TestUtils.getRepositoryContent(connection, contentGraph);
        assertEquals(expected, actual);
    }    

    @Test
    @Order(110)
    public void shouldProcessBlogCreated() throws Exception {    

        TestServicesUtils.resetBlogContentToVersionOne(contentService, historyService);

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();
        
        ValueFactory vf = connection.getValueFactory();
        IRI contentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        connection.clear();

        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/content-pre-webhook-blog-created-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, contentGraph);

        FileSystemResource event = new FileSystemResource("./src/test/resources/events/webhook-blog-created.ttl");
        Model eventModel = Rio.parse(event.getInputStream(), RDFFormat.TURTLE);

        processor.onEvent(eventModel);

        String expected = Files.readString(Paths.get("./src/test/resources/repositories/client/content-post-webhook-blog-created-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl"));
        String actual = TestUtils.getRepositoryContent(connection, contentGraph);
        assertEquals(expected, actual);
    }

    @Test
    @Order(120)
    public void shouldProcessBlogUpdated() throws Exception {

        TestServicesUtils.resetBlogContentToVersionTwoWithUpdatedContent(contentService, historyService);

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();
        
        ValueFactory vf = connection.getValueFactory();
        IRI contentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        connection.clear();

        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/content-pre-webhook-blog-updated-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, contentGraph);

        FileSystemResource event = new FileSystemResource("./src/test/resources/events/webhook-blog-updated.ttl");
        Model eventModel = Rio.parse(event.getInputStream(), RDFFormat.TURTLE);

        processor.onEvent(eventModel);

        String expected = Files.readString(Paths.get("./src/test/resources/repositories/client/content-post-webhook-blog-updated-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl"));
        String actual = TestUtils.getRepositoryContent(connection, contentGraph);
        assertEquals(expected, actual);
    }

    @Test
    @Order(130)
    public void shouldProcessBlogTrashed() throws Exception {

        TestServicesUtils.resetBlogContentToVersionTwoWithUpdatedContent(contentService, historyService);

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();
        
        ValueFactory vf = connection.getValueFactory();
        IRI contentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        connection.clear();

        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/content-pre-webhook-blog-trashed-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, contentGraph);

        FileSystemResource event = new FileSystemResource("./src/test/resources/events/webhook-blog-trashed.ttl");
        Model eventModel = Rio.parse(event.getInputStream(), RDFFormat.TURTLE);

        processor.onEvent(eventModel);

        String expected = Files.readString(Paths.get("./src/test/resources/repositories/client/content-post-webhook-blog-trashed-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl"));
        String actual = TestUtils.getRepositoryContent(connection, contentGraph);
        assertEquals(expected, actual);
    }

    @Test
    @Order(140)
    public void shouldProcessBlogRestored() throws Exception {

        TestServicesUtils.resetBlogContentToVersionTwoWithUpdatedContent(contentService, historyService);

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();
        
        ValueFactory vf = connection.getValueFactory();
        IRI contentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        connection.clear();

        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/content-pre-webhook-blog-restored-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, contentGraph);

        FileSystemResource event = new FileSystemResource("./src/test/resources/events/webhook-blog-restored.ttl");
        Model eventModel = Rio.parse(event.getInputStream(), RDFFormat.TURTLE);

        processor.onEvent(eventModel);

        String expected = Files.readString(Paths.get("./src/test/resources/repositories/client/content-post-webhook-blog-restored-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl"));
        String actual = TestUtils.getRepositoryContent(connection, contentGraph);
        assertEquals(expected, actual);
    }

    @Test
    @Order(150)
    public void shouldProcessBlogRemoved() throws Exception {

        TestServicesUtils.resetBlogContentToVersionTwoWithUpdatedContent(contentService, historyService);

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();
        
        ValueFactory vf = connection.getValueFactory();
        IRI contentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        connection.clear();

        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/content-pre-webhook-blog-removed-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, contentGraph);

        FileSystemResource event = new FileSystemResource("./src/test/resources/events/webhook-blog-removed.ttl");
        Model eventModel = Rio.parse(event.getInputStream(), RDFFormat.TURTLE);

        processor.onEvent(eventModel);

        String expected = Files.readString(Paths.get("./src/test/resources/repositories/client/content-post-webhook-blog-removed-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl"));
        String actual = TestUtils.getRepositoryContent(connection, contentGraph);
        assertEquals(expected, actual);
    }
}
