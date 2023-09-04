package com.dalstonsemantics.confluence.semantics.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {TestAtlassianConnectContextArgumentResolverConfigurer.class, TestProviders.class, TestRepositories.class, TestServices.class})
@TestMethodOrder(OrderAnnotation.class)
public class RelationMacroControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired 
    @Qualifier("TaxonomyRepositoryPool") 
    private Rdf4jRepositoryPool taxonomyRepositoryPool;
    
    @BeforeAll
    public void setUpTaxonomyVersion() throws IOException {

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        connection.clear();

        ValueFactory vf = connection.getValueFactory();
        IRI taxonomyVersion = vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy-version/927294f7-0a9f-3d01-8120-b3ca3a45df38");
        IRI versionZero = vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        FileSystemResource taxonomyVersionsData = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-version-default-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(taxonomyVersionsData.getInputStream(), RDFFormat.TURTLE, taxonomyVersion);

        FileSystemResource anzsicData = new FileSystemResource("./src/test/resources/requests/anzsic.ttl");
        connection.add(anzsicData.getInputStream(), RDFFormat.TURTLE, versionZero);
    }

    @Test
    @Order(10)
    public void shouldRenderRelationMacro() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/relation?uri=https://www.abs.gov.au/ausstats/anzsic/01&lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/RelationMacro.txt")), result.getBody());
        assertEquals("max-age=3600, must-revalidate, no-transform", result.getHeaders().getCacheControl());
    }

    @Test
    @Order(10)
    public void shouldRenderRelationMacroNotFound() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/relation?uri=https://www.abs.gov.au/ausstats/anzsic/XXXXX&lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/RelationMacroNotFound.txt")), result.getBody());
        assertEquals("max-age=3600, must-revalidate, no-transform", result.getHeaders().getCacheControl());
    }

    @Test
    @Order(10)
    public void shouldRenderRelationMacroEditor() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/relation-editor?lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/RelationMacroEditor.txt")), result.getBody());
    }
}