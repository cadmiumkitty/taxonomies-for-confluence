package com.dalstonsemantics.confluence.semantics.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {TestAtlassianConnectContextArgumentResolverConfigurer.class, TestProviders.class, TestRepositories.class, TestServices.class})
@TestMethodOrder(OrderAnnotation.class)
public class TocMacroControllerTest {

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

    @BeforeAll
    public void setUpTaxonomyVersionAndContent() throws Exception {

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        connection.clear();

        ValueFactory vf = connection.getValueFactory();
        IRI taxonomyVersion = vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy-version/927294f7-0a9f-3d01-8120-b3ca3a45df38");
        IRI versionZero = vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38");
        IRI contentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        FileSystemResource taxonomyVersionsData = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-version-default-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(taxonomyVersionsData.getInputStream(), RDFFormat.TURTLE, taxonomyVersion);

        FileSystemResource anzsicData = new FileSystemResource("./src/test/resources/requests/anzsic.ttl");
        connection.add(anzsicData.getInputStream(), RDFFormat.TURTLE, versionZero);

        FileSystemResource contentGraphData = new FileSystemResource("./src/test/resources/repositories/client/content-default-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(contentGraphData.getInputStream(), RDFFormat.TURTLE, contentGraph);
    }

    @Test
    @Order(10)
    public void shouldRenderTocMacroNonTransitive() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(
            String.format("http://localhost:%d/toc?transitive=false,false&predicate=http://purl.org/dc/terms/subject,http://purl.org/dc/terms/relation&object=https://www.abs.gov.au/ausstats/anzsic/B,https://www.abs.gov.au/ausstats/anzsic/B&lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TocMacroNonTransitive.txt")), result.getBody());
    }

    @Test
    @Order(10)
    public void shouldRenderTocMacroTransitive() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(
            String.format("http://localhost:%d/toc?transitive=true,true&predicate=http://purl.org/dc/terms/subject,http://purl.org/dc/terms/relation&object=https://www.abs.gov.au/ausstats/anzsic/B,https://www.abs.gov.au/ausstats/anzsic/A&lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TocMacroTransitive.txt")), result.getBody());
    }

    @Test
    @Order(20)
    public void shouldRenderTocMacroEditor() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/toc-editor?lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TocMacroEditor.txt")), result.getBody());
    }
}