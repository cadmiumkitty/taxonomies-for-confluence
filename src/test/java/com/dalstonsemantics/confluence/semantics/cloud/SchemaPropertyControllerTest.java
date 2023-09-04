package com.dalstonsemantics.confluence.semantics.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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

import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {TestAtlassianConnectContextArgumentResolverConfigurer.class, TestProviders.class, TestRepositories.class, TestServices.class})
@TestMethodOrder(OrderAnnotation.class)
public class SchemaPropertyControllerTest {

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
    public void setUpTaxonomyVersion() throws IOException {

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

        FileSystemResource dataGovernanceData = new FileSystemResource("./src/test/resources/requests/data-governance.ttl");
        connection.add(dataGovernanceData.getInputStream(), RDFFormat.TURTLE, versionZero);

        FileSystemResource schemaDotOrgData = new FileSystemResource("./src/test/resources/requests/schemaorg-current-https.ttl");
        connection.add(schemaDotOrgData.getInputStream(), RDFFormat.TURTLE, versionZero);

        FileSystemResource rdfData = new FileSystemResource("./src/test/resources/requests/22-rdf-syntax-ns.ttl");
        connection.add(rdfData.getInputStream(), RDFFormat.TURTLE, versionZero);

        FileSystemResource rdfsData = new FileSystemResource("./src/test/resources/requests/rdf-schema.ttl");
        connection.add(rdfsData.getInputStream(), RDFFormat.TURTLE, versionZero);


        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/content-default-schema-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, contentGraph);
    }

    @Test
    @Order(20)
    public void shouldReturnSchemaDefiningResourcesForProperties() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(
            String.format("http://localhost:%d/schema/defining-resources-for-properties?context={context}", port),
            String.class,
            "https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SchemaDefiningResourcesForProperties.txt")), result.getBody());
    }

    @Test
    @Order(30)
    public void shouldReturnSchemaTopProperties() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(
            String.format("http://localhost:%d/schema/top-properties?context={context}", port),
            String.class,
            "https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SchemaTopProperties.txt")), result.getBody());
    }

    @Test
    @Order(40)
    public void shouldReturnSchemaTopPropertiesForDefiningResource() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(
            String.format("http://localhost:%d/schema/top-properties-for-defining-resource?resource={resource}&context={context}", port),    
            String.class,
            "http://www.w3.org/2000/01/rdf-schema#",
            "https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SchemaTopPropertiesForDefiningResource.txt")), result.getBody());
    }

    @Test
    @Order(90)
    public void shouldReturnSchemaSubProperties() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(
            String.format("http://localhost:%d/schema/sub-properties?property={property}&context={context}", port),
            String.class,
            "https://schema.org/suggestedAnswer",
            "https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38"
            );

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SchemaSubProperties.txt")), result.getBody());
    }

    @Test
    @Order(90)
    public void shouldReturnSchemaSubPropertiesForDefiningResource() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(
            String.format("http://localhost:%d/schema/sub-properties-for-defining-resource?resource={resource}&property={property}&context={context}", port),            
            String.class,
            "https://dalstonsemantics.com/dg/schema",
            "https://dalstonsemantics.com/dg/ownerOfDataElement",
            "https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SchemaSubPropertiesForDefiningResource.txt")), result.getBody());
    }

    @Test
    @Order(140)
    public void shouldReturnSchemaContentWithPropertyAsPredicate() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(
            String.format("http://localhost:%d/schema/content-with-property-as-predicate?property={property}&context={context}", port), 
            String.class,
            "https://schema.org/interpretedAsClaim",
            "https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SchemaContentWithPropertyAsPredicate.txt")), result.getBody());
    }
}