package com.dalstonsemantics.confluence.semantics.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.net.URISyntaxException;
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
public class TaxonomyControllerTest {

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

        FileSystemResource contentGraphData = new FileSystemResource("./src/test/resources/repositories/client/content-default-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(contentGraphData.getInputStream(), RDFFormat.TURTLE, contentGraph);
    }

    @Test
    @Order(10)
    public void shouldRenderSubjectByline() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/subject-byline?lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SubjectByline.txt")), result.getBody());
    }

    @Test
    @Order(10)
    public void shouldRenderTypeByline() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/type-byline?lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TypeByline.txt")), result.getBody());
    }

    @Test
    @Order(10)
    public void shouldRenderTaxonomyAdminPage() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy-admin-page?lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyAdminPage.txt")), result.getBody());
    }

    @Test
    @Order(10)
    public void shouldRenderTaxonomyPage() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy-page?lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyPage.txt")), result.getBody());
    }

    @Test
    @Order(10)
    public void shouldRenderTaxonomyPageWithCorrectContentSecurityPolicyHeader() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy-page?lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals("script-src 'self' connect-cdn.atl-paas.net; form-action 'self'", result.getHeaders().get("Content-Security-Policy").get(0));
    }

    @Test
    @Order(10)
    public void shouldRenderStaticContentWithCorrectCacheHeader() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/images/taxonomies-empty.png", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals("max-age=31536000, public", result.getHeaders().getCacheControl());
    }

    @Test
    @Order(10)
    public void shouldRenderStaticContentWithCorrectContentSecurityPolicyHeader() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/images/taxonomies-empty.png", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertNull(result.getHeaders().get("Content-Security-Policy"));
    }

    @Test
    @Order(30)
    public void shouldGetTaxonomyContent() throws IOException, URISyntaxException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/content?concept=https://www.abs.gov.au/ausstats/anzsic/A", port), String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyContent.txt")), result.getBody());
    }

    @Test
    @Order(40)
    public void shouldReturnTaxonomyConceptSchemes() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/conceptscheme", port), String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyConceptSchemes.txt")), result.getBody());
    }

    @Test
    @Order(40)
    public void shouldReturnTaxonomyConceptSchemesWithContext() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/conceptscheme?context=https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38", port), String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyConceptSchemes.txt")), result.getBody());
    }

    @Test
    @Order(40)
    public void shouldReturnTaxonomyTopConcepts() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/topconcept?scheme=https://www.abs.gov.au/ausstats/anzsic/scheme", port), String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyTopConcepts.txt")), result.getBody());
    }

    @Test
    @Order(40)
    public void shouldReturnTaxonomyTopConceptsWithContext() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/topconcept?scheme=https://www.abs.gov.au/ausstats/anzsic/scheme&context=https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38", port), String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyTopConcepts.txt")), result.getBody());
    }

    @Test
    @Order(40)
    public void shouldReturnTaxonomyNarrowerConcepts() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/narrowerconcept?broader=https://www.abs.gov.au/ausstats/anzsic/A", port), String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyNarrowerConcepts.txt")), result.getBody());
    }

    @Test
    @Order(40)
    public void shouldReturnTaxonomyNarrowerConceptsWithContext() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/narrowerconcept?broader=https://www.abs.gov.au/ausstats/anzsic/A&context=https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38", port), String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyNarrowerConcepts.txt")), result.getBody());
    }

    @Test
    @Order(40)
    public void shouldReturnTaxonomyConcept() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/concept?uri=https://www.abs.gov.au/ausstats/anzsic/01", port), String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyConcept.txt")), result.getBody());
    }

    @Test
    @Order(40)
    public void shouldReturnTaxonomyConceptWithContext() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/concept?uri=https://www.abs.gov.au/ausstats/anzsic/01&context=https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38", port), String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyConcept.txt")), result.getBody());
    }

    @Test
    @Order(40)
    public void shouldQueryTaxonomyConcepts() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/search?q=out", port), String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyConcepts.txt")), result.getBody());
    }

    @Test
    @Order(40)
    public void shouldGetContentProvenance() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/provenance?resource=https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38-100", port), String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyContentProvenance.txt")), result.getBody());
    }

    @Test
    @Order(50)
    public void shouldReturnEmptyGraphForEmptyContentGraph() throws IOException, URISyntaxException {

        RepositoryConnection connection = taxonomyRepositoryPool.getRepository("taxonomy").getConnection();

        ValueFactory vf = connection.getValueFactory();
        IRI contentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        connection.clear(contentGraph);

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/taxonomy/content?concept=https://www.abs.gov.au/ausstats/anzsic/A", port), String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/TaxonomyContentEmpty.txt")), result.getBody());
    }
}