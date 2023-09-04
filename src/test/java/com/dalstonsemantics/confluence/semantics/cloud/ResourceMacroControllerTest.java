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
public class ResourceMacroControllerTest {

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

        FileSystemResource dataGovernanceData = new FileSystemResource("./src/test/resources/requests/data-governance.ttl");
        connection.add(dataGovernanceData.getInputStream(), RDFFormat.TURTLE, versionZero);
    }

    @Test
    @Order(10)
    public void shouldRenderConceptResourceMacro() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/resource?uri=https://www.abs.gov.au/ausstats/anzsic/01&lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/ResourceMacroConcept.txt")), result.getBody());
        assertEquals("max-age=3600, must-revalidate, no-transform", result.getHeaders().getCacheControl());
    }

    @Test
    @Order(20)
    public void shouldRenderClassResourceMacro() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/resource?uri=https://dalstonsemantics.com/dg/DataDomain&lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/ResourceMacroClass.txt")), result.getBody());
        assertEquals("max-age=3600, must-revalidate, no-transform", result.getHeaders().getCacheControl());
    }

    @Test
    @Order(30)
    public void shouldRenderPropertyResourceMacro() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/resource?uri=https://dalstonsemantics.com/dg/ownerOfDataDomain&lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/ResourceMacroProperty.txt")), result.getBody());
        assertEquals("max-age=3600, must-revalidate, no-transform", result.getHeaders().getCacheControl());
    }

    @Test
    @Order(40)
    public void shouldRenderResourcenMacroNotFound() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/resource?uri=https://www.abs.gov.au/ausstats/anzsic/XXXXX&lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/ResourceMacroNotFound.txt")), result.getBody());
        assertEquals("max-age=3600, must-revalidate, no-transform", result.getHeaders().getCacheControl());
    }

    @Test
    @Order(50)
    public void shouldRenderResourceMacroEditor() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/resource-editor?lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/ResourceMacroEditor.txt")), result.getBody());
    }
}