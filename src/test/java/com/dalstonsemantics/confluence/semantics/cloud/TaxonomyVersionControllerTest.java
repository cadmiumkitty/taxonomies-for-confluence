package com.dalstonsemantics.confluence.semantics.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {TestAtlassianConnectContextArgumentResolverConfigurer.class, TestProviders.class, TestRepositories.class, TestServices.class})
@TestMethodOrder(OrderAnnotation.class)
public class TaxonomyVersionControllerTest {

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

    @BeforeEach
    public void setUpTaxonomyVersion() throws IOException {

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();
        connection.clear();
    }

    @Test
    @Order(10)
    public void shouldGetTaxonomyVersion() throws IOException, URISyntaxException {

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        ValueFactory vf = connection.getValueFactory();
        IRI taxonomyVersion = vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy-version/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        FileSystemResource taxonomyVersionsData = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-version-default-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(taxonomyVersionsData.getInputStream(), RDFFormat.TURTLE, taxonomyVersion);

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/version", port), String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/taxonomy-version-controller/GetTaxonomyVersion.txt")), result.getBody());
    }

    @Test
    @Order(20)
    public void shouldPostImportFile() throws IOException, URISyntaxException {

        // Create versions

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        ValueFactory vf = connection.getValueFactory();
        IRI taxonomyVersion = vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy-version/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        FileSystemResource taxonomyVersionsData = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-version-default-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(taxonomyVersionsData.getInputStream(), RDFFormat.TURTLE, taxonomyVersion);

        // Trigger transition

        FileSystemResource anzsic = new FileSystemResource("./src/test/resources/requests/anzsic.ttl");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", anzsic);

        ResponseEntity<String> result = this.restTemplate.postForEntity(String.format("http://localhost:%d/taxonomy/version/draft/import-file", port), body, String.class);

        assertEquals(200, result.getStatusCode().value());

        // Check version status

        ResponseEntity<String> versionResult = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/version", port), String.class);

        assertEquals(200, versionResult.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/taxonomy-version-controller/GetTaxonomyVersionPutImportFile.txt")), versionResult.getBody());
    }

    @Test
    @Order(25)
    public void shouldPostImportFileFromError() throws IOException, URISyntaxException {

        // Create versions

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        ValueFactory vf = connection.getValueFactory();
        IRI taxonomyVersion = vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy-version/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        FileSystemResource taxonomyVersionsData = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-version-default-with-error-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(taxonomyVersionsData.getInputStream(), RDFFormat.TURTLE, taxonomyVersion);

        // Trigger transition

        FileSystemResource anzsic = new FileSystemResource("./src/test/resources/requests/anzsic.ttl");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", anzsic);

        ResponseEntity<String> result = this.restTemplate.postForEntity(String.format("http://localhost:%d/taxonomy/version/draft/import-file", port), body, String.class);

        assertEquals(200, result.getStatusCode().value());

        // Check version status

        ResponseEntity<String> versionResult = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/version", port), String.class);

        assertEquals(200, versionResult.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/taxonomy-version-controller/GetTaxonomyVersionPutImportFile.txt")), versionResult.getBody());
    }

    @Test
    @Order(30)
    public void shouldPostImportResource() throws IOException, URISyntaxException {

        // Create versions

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        ValueFactory vf = connection.getValueFactory();
        IRI taxonomyVersion = vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy-version/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        FileSystemResource taxonomyVersionsData = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-version-default-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(taxonomyVersionsData.getInputStream(), RDFFormat.TURTLE, taxonomyVersion);

        // Trigger transition

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("scheme", "https://dalstonsemantics.com/ns/au/gov/abs/anzsic/scheme");
        body.add("scheme", "https://dalstonsemantics.com/ns/org/isbn-international/978-0124158290/scheme");
        body.add("scheme", "https://dalstonsemantics.com/taxonomy/sdlc/scheme");
        body.add("scheme", "https://dalstonsemantics.com/ns/org/opengroup/pubs/architecture/togaf9-doc/arch/chap37.html#scheme");
        body.add("scheme", "http://www.semanticweb.org/ontologies/2020/4/VocabularyTOGAFContentMetamodel.skos#scheme");

        ResponseEntity<String> result = this.restTemplate.postForEntity(
                String.format("http://localhost:%d/taxonomy/version/draft/import-catalog", port), 
                body, 
                String.class);

        assertEquals(200, result.getStatusCode().value());

        // Check version status

        ResponseEntity<String> versionResult = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/version", port), String.class);

        assertEquals(200, versionResult.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/taxonomy-version-controller/GetTaxonomyVersionPutImportCatalog.txt")), versionResult.getBody());
    }

    @Test
    @Order(40)
    public void shouldPostCalculateContentImpact() throws IOException, URISyntaxException {

        // Create versions

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        ValueFactory vf = connection.getValueFactory();
        IRI taxonomyVersion = vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy-version/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        FileSystemResource taxonomyVersionsData = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-version-default-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(taxonomyVersionsData.getInputStream(), RDFFormat.TURTLE, taxonomyVersion);

        // Trigger transition

        RequestEntity<String> contentCreatedRequestEntity = RequestEntity
            .post(new URL(String.format("http://localhost:%d/taxonomy/version/draft/calculate-content-impact", port)).toURI())
            .contentType(MediaType.APPLICATION_JSON)
            .body(""); 
        ResponseEntity<String> contentCreatedResult = restTemplate.exchange(contentCreatedRequestEntity, String.class);

        assertEquals(200, contentCreatedResult.getStatusCode().value());

        // Check version status

        ResponseEntity<String> versionResult = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/version", port), String.class);

        assertEquals(200, versionResult.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/taxonomy-version-controller/GetTaxonomyVersionPutCalculateContentImpact.txt")), versionResult.getBody());
    }

    @Test
    @Order(50)
    public void shouldPostTransitionToCurrent() throws IOException, URISyntaxException {

        // Create versions

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        ValueFactory vf = connection.getValueFactory();
        IRI taxonomyVersion = vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy-version/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        FileSystemResource taxonomyVersionsData = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-version-awaiting_transition_to_current-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(taxonomyVersionsData.getInputStream(), RDFFormat.TURTLE, taxonomyVersion);

        // Trigger transition

        RequestEntity<String> contentCreatedRequestEntity = RequestEntity
            .post(new URL(String.format("http://localhost:%d/taxonomy/version/draft/transition-to-current", port)).toURI())
            .contentType(MediaType.APPLICATION_JSON)
            .body(""); 
        ResponseEntity<String> contentCreatedResult = restTemplate.exchange(contentCreatedRequestEntity, String.class);

        assertEquals(200, contentCreatedResult.getStatusCode().value());

        // Check version status

        ResponseEntity<String> versionResult = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/version", port), String.class);

        assertEquals(200, versionResult.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/taxonomy-version-controller/GetTaxonomyVersionPutTransitionToCurrent.txt")), versionResult.getBody());
    }

    @Test
    @Order(60)
    public void shouldPostCancelTransitionToCurrent() throws IOException, URISyntaxException {

        // Create versions

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        ValueFactory vf = connection.getValueFactory();
        IRI taxonomyVersion = vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy-version/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        FileSystemResource taxonomyVersionsData = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-version-awaiting_transition_to_current-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(taxonomyVersionsData.getInputStream(), RDFFormat.TURTLE, taxonomyVersion);

        // Trigger transition

        RequestEntity<String> contentCreatedRequestEntity = RequestEntity
            .post(new URL(String.format("http://localhost:%d/taxonomy/version/draft/cancel-transition-to-current", port)).toURI())
            .contentType(MediaType.APPLICATION_JSON)
            .body(""); 
        ResponseEntity<String> contentCreatedResult = restTemplate.exchange(contentCreatedRequestEntity, String.class);

        assertEquals(200, contentCreatedResult.getStatusCode().value());

        // Check version status

        ResponseEntity<String> versionResult = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/version", port), String.class);

        assertEquals(200, versionResult.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/taxonomy-version-controller/GetTaxonomyVersionPutCancelTransitionToCurrent.txt")), versionResult.getBody());
    }

    @Test
    @Order(70)
    public void shouldPostClear() throws IOException, URISyntaxException {

        // Create versions

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        ValueFactory vf = connection.getValueFactory();
        IRI taxonomyVersion = vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy-version/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        FileSystemResource taxonomyVersionsData = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-version-default-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(taxonomyVersionsData.getInputStream(), RDFFormat.TURTLE, taxonomyVersion);

        // Trigger transition

        RequestEntity<String> contentCreatedRequestEntity = RequestEntity
            .post(new URL(String.format("http://localhost:%d/taxonomy/version/draft/clear", port)).toURI())
            .contentType(MediaType.APPLICATION_JSON)
            .body(""); 
        ResponseEntity<String> contentCreatedResult = restTemplate.exchange(contentCreatedRequestEntity, String.class);

        assertEquals(200, contentCreatedResult.getStatusCode().value());

        // Check version status

        ResponseEntity<String> versionResult = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/version", port), String.class);

        assertEquals(200, versionResult.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/taxonomy-version-controller/GetTaxonomyVersionPutClear.txt")), versionResult.getBody());
    }

}