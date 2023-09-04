package com.dalstonsemantics.confluence.semantics.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
public class SparqlMacroControllerTest {

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
        IRI materializedContentGraph = vf.createIRI("https://tfc.dalstonsemantics.com/materialized-content/927294f7-0a9f-3d01-8120-b3ca3a45df38");

        FileSystemResource taxonomyVersionsData = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-version-default-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(taxonomyVersionsData.getInputStream(), RDFFormat.TURTLE, taxonomyVersion);

        FileSystemResource anzsicData = new FileSystemResource("./src/test/resources/requests/anzsic.ttl");
        connection.add(anzsicData.getInputStream(), RDFFormat.TURTLE, versionZero);

        FileSystemResource materializedContentGraphData = new FileSystemResource("./src/test/resources/repositories/client/materialized-content-post-content-graph-materialized-sparql-macro-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(materializedContentGraphData.getInputStream(), RDFFormat.TURTLE, materializedContentGraph);
    }

    @Test
    @Order(10)
    public void shouldRenderSparqlMacroUriText() throws IOException {

        String q = URLEncoder.encode(
                """
                PREFIX dcterms: <http://purl.org/dc/terms/>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                SELECT * 
                WHERE { 
                    ?page a team:page ;
                        dcterms:title ?title ;
                        dcterms:source ?source .
                }
                """, StandardCharsets.UTF_8.toString());

        ResponseEntity<String> result = this.restTemplate.getForEntity(
                String.format("http://localhost:%d/sparql-macro?pageId=100&pageType=page&q=%s&lic=active", port, q), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SparqlMacroUriText.txt")), result.getBody());
    }

    @Test
    @Order(20)
    public void shouldRenderSparqlMacroContentUriText() throws IOException {

        String q = URLEncoder.encode(
                """
                PREFIX dcterms: <http://purl.org/dc/terms/>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                SELECT * 
                WHERE { 
                    ?page a team:page ;
                        dcterms:title ?title ;
                        dcterms:source ?source ;
                        team:contentId ?content_id.
                }
                """, StandardCharsets.UTF_8.toString());

        ResponseEntity<String> result = this.restTemplate.getForEntity(
                String.format("http://localhost:%d/sparql-macro?pageId=100&pageType=page&q=%s&lic=active", port, q), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SparqlMacroContentUriText.txt")), result.getBody());
    }

    @Test
    @Order(30)
    public void shouldRenderSparqlMacroUserUri() throws IOException {

        String q = URLEncoder.encode(
                """
                PREFIX dcterms: <http://purl.org/dc/terms/>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                PREFIX prov: <http://www.w3.org/ns/prov#>
                SELECT * 
                WHERE { 
                    ?agent a prov:Agent ;
                        team:accountId ?account_id .
                }
                """, StandardCharsets.UTF_8.toString());

        ResponseEntity<String> result = this.restTemplate.getForEntity(
                String.format("http://localhost:%d/sparql-macro?pageId=100&pageType=page&q=%s&lic=active", port, q), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SparqlMacroUserUri.txt")), result.getBody());
    }

    @Test
    @Order(40)
    public void shouldRenderSparqlMacroExternalService() throws IOException {

        String q = URLEncoder.encode(
            """
            PREFIX lrppi: <http://landregistry.data.gov.uk/def/ppi/>
            PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
            PREFIX lrcommon: <http://landregistry.data.gov.uk/def/common/>
            PREFIX sdo: <https://schema.org/>

            SELECT * 
            WHERE {
                {
                    SELECT ?postcode
                    WHERE {
                        ?this sdo:postalCode ?postcode .
                    }
                }
                SERVICE <https://landregistry.data.gov.uk/landregistry/query> { 
                    ?addr lrcommon:postcode ?postcode.
                
                    ?transx lrppi:propertyAddress ?addr ;
                            lrppi:pricePaid ?amount ;
                            lrppi:transactionDate ?date ;
                            lrppi:transactionCategory/skos:prefLabel ?category.
                
                    OPTIONAL { ?addr lrcommon:county ?county }
                    OPTIONAL { ?addr lrcommon:paon ?paon }
                    OPTIONAL { ?addr lrcommon:saon ?saon }
                    OPTIONAL { ?addr lrcommon:street ?street }
                    OPTIONAL { ?addr lrcommon:town ?town }
                }
            }
            ORDER BY DESC(?date)
            LIMIT 5
            """, StandardCharsets.UTF_8.toString());

        ResponseEntity<String> result = this.restTemplate.getForEntity(
                String.format("http://localhost:%d/sparql-macro?pageId=100&pageType=page&q=%s&lic=active", port, q), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SparqlMacroExternalService.txt")), result.getBody());
    }

    @Test
    @Order(50)
    public void shouldRenderSparqlMacroUriTextWithMissingPageIdPageType() throws IOException {

        String q = URLEncoder.encode(
                """
                PREFIX dcterms: <http://purl.org/dc/terms/>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                SELECT * 
                WHERE { 
                    ?page a team:page ;
                        dcterms:title ?title ;
                        dcterms:source ?source .
                }
                """, StandardCharsets.UTF_8.toString());

        ResponseEntity<String> result = this.restTemplate.getForEntity(
                String.format("http://localhost:%d/sparql-macro?q=%s&lic=active", port, q), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SparqlMacroUriText.txt")), result.getBody());
    }

    @Test
    @Order(100)
    public void shouldRenderSparqlMacroEditor() throws IOException {

        ResponseEntity<String> result = this.restTemplate.getForEntity(String.format("http://localhost:%d/sparql-macro-editor?lic=active", port), String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SparqlMacroEditor.txt")), result.getBody());
    }
}