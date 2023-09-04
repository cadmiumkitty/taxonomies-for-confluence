package com.dalstonsemantics.confluence.semantics.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {TestAtlassianConnectContextArgumentResolverConfigurer.class, TestProviders.class, TestRepositories.class, TestServices.class})
@TestMethodOrder(OrderAnnotation.class)
public class SparqlControllerTest {

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
        ValueFactory vf = connection.getValueFactory();

        connection.clear();

        connection.begin();

        FileSystemResource versionZero = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(versionZero.getInputStream(), RDFFormat.TURTLE, vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38"));

        FileSystemResource versionOne = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-927294f7-0a9f-3d01-8120-b3ca3a45df38-1.ttl");
        connection.add(versionOne.getInputStream(), RDFFormat.TURTLE, vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38-1"));

        FileSystemResource taxonomyVersion = new FileSystemResource("./src/test/resources/repositories/client/taxonomy-version-calculating_content_impact-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(taxonomyVersion.getInputStream(), RDFFormat.TURTLE, vf.createIRI("https://tfc.dalstonsemantics.com/taxonomy-version/927294f7-0a9f-3d01-8120-b3ca3a45df38"));

        FileSystemResource content = new FileSystemResource("./src/test/resources/repositories/client/materialized-content-post-content-graph-materialized-927294f7-0a9f-3d01-8120-b3ca3a45df38.ttl");
        connection.add(content.getInputStream(), RDFFormat.TURTLE, vf.createIRI("https://tfc.dalstonsemantics.com/materialized-content/927294f7-0a9f-3d01-8120-b3ca3a45df38"));

        connection.commit();
    }

    @Test
    public void shouldAuthenticateWithCorrectIssAndToken() throws IOException {

        String uriEscapedQueryString = URLEncoder.encode(
                """
                SELECT * 
                WHERE { 
                    ?s a <https://dalstonsemantics.com/ns/com/atlassian/page> . 
                }
                """, StandardCharsets.UTF_8.toString());
        ResponseEntity<String> result = this.restTemplate.getForEntity(
                "http://localhost:%d/sparql/12345?token=YYY&query=%s".formatted(port, uriEscapedQueryString), 
                String.class);
        
        assertEquals(401, result.getStatusCode().value());
    }

    @Test
    public void shouldAuthenticateWithCorrectToken() throws IOException {

        String uriEscapedQueryString = URLEncoder.encode(
                """
                SELECT * 
                WHERE { 
                    ?s a <https://dalstonsemantics.com/ns/com/atlassian/page> . 
                }
                """, StandardCharsets.UTF_8.toString());
        ResponseEntity<String> result = this.restTemplate.getForEntity(
                "http://localhost:%d/sparql/927294f7-0a9f-3d01-8120-b3ca3a45df38?token=YYY&query=%s".formatted(port, uriEscapedQueryString), 
                String.class);
        
        assertEquals(401, result.getStatusCode().value());
    }

    @Test
    public void shouldExecuteSparqlQuery() throws IOException {

        String uriEscapedQueryString = URLEncoder.encode(
                """
                SELECT * 
                WHERE { 
                    ?s a <https://dalstonsemantics.com/ns/com/atlassian/page> . 
                }
                """, StandardCharsets.UTF_8.toString());
        ResponseEntity<String> result = this.restTemplate.getForEntity(
                "http://localhost:%d/sparql/927294f7-0a9f-3d01-8120-b3ca3a45df38?token=XXX&query=%s".formatted(port, uriEscapedQueryString), 
                String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals("application/sparql-results+json", result.getHeaders().getContentType().toString());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SparqlQueryOk.txt")), result.getBody());
    }

    @Test
    public void shouldExecuteSparqlQueryOnlyAgainstContentGraph() throws IOException {

        String uriEscapedQueryString = URLEncoder.encode(
                """
                SELECT * 
                WHERE { 
                    GRAPH <https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38> { 
                        ?s a <http://www.w3.org/2004/02/skos/core#Concept> . 
                    } 
                }
                """, StandardCharsets.UTF_8.toString());
        ResponseEntity<String> result = this.restTemplate.getForEntity(
                "http://localhost:%d/sparql/927294f7-0a9f-3d01-8120-b3ca3a45df38?token=XXX&query=%s".formatted(port, uriEscapedQueryString), 
                String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals("application/sparql-results+json", result.getHeaders().getContentType().toString());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SparqlQueryTaxonomyGraphOk.txt")), result.getBody());
    }

    @Test
    public void shouldExecuteSparqlQueryFromClient() throws Exception {

        Repository repository = new SPARQLRepository(
                "http://localhost:%d/sparql/927294f7-0a9f-3d01-8120-b3ca3a45df38?token=XXX".formatted(port));
        RepositoryConnection connection = repository.getConnection();

        TupleQuery query = connection.prepareTupleQuery(
                """
                SELECT * 
                WHERE { 
                    ?s a <https://dalstonsemantics.com/ns/com/atlassian/page> . 
                }
                """);
        TupleQueryResult result = query.evaluate();
        
        long resultCount = result.stream().count();
        assertEquals(4, resultCount);
    }

    @Test
    public void shouldExecuteFederatedSparqlQueryFromClient() throws Exception {

        Repository repository = new SailRepository(new MemoryStore());
        RepositoryConnection connection = repository.getConnection();

        TupleQuery query = connection.prepareTupleQuery(
                """
                SELECT * 
                WHERE { 
                    SERVICE <http://localhost:%d/sparql/927294f7-0a9f-3d01-8120-b3ca3a45df38?token=XXX> { 
                        ?s a <https://dalstonsemantics.com/ns/com/atlassian/page> . 
                    } 
                }
                """.formatted(port));
        TupleQueryResult result = query.evaluate();
        
        long resultCount = result.stream().count();
        assertEquals(4, resultCount);        
    }

    @Test
    public void shouldExecuteSparqlQueryWithFederation() throws Exception {

        String uriEscapedQueryString = URLEncoder.encode(
                """
                PREFIX lrppi: <http://landregistry.data.gov.uk/def/ppi/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                PREFIX lrcommon: <http://landregistry.data.gov.uk/def/common/>

                SELECT * 
                WHERE { 
                    SERVICE <https://landregistry.data.gov.uk/landregistry/query> { 
                        SELECT ?paon ?saon ?street ?town ?county ?postcode ?amount ?date ?category
                        WHERE
                        {
                          VALUES ?postcode { "E8 4DS"^^xsd:string }
                        
                          ?addr lrcommon:postcode ?postcode.
                        
                          ?transx lrppi:propertyAddress ?addr ;
                                  lrppi:pricePaid ?amount ;
                                  lrppi:transactionDate ?date ;
                                  lrppi:transactionCategory/skos:prefLabel ?category.
                        
                          OPTIONAL { ?addr lrcommon:county ?county}
                          OPTIONAL { ?addr lrcommon:paon ?paon }
                          OPTIONAL { ?addr lrcommon:saon ?saon }
                          OPTIONAL { ?addr lrcommon:street ?street }
                          OPTIONAL { ?addr lrcommon:town ?town }
                        }
                        ORDER BY DESC(?date)
                        LIMIT 5
                    }
                }
                """, StandardCharsets.UTF_8.toString());

        ResponseEntity<String> result = this.restTemplate.getForEntity(
                "http://localhost:%d/sparql/927294f7-0a9f-3d01-8120-b3ca3a45df38?token=XXX&query=%s".formatted(port, uriEscapedQueryString), 
                String.class);
                
        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    public void shouldNotExecuteSparqlQueryWithCustomFunction() throws Exception {

        String uriEscapedQueryString = URLEncoder.encode(
                """
                PREFIX cfn: <http://example.org/custom-function/> 
                SELECT * 
                WHERE { 
                    ?s a <https://dalstonsemantics.com/ns/com/atlassian/page> . 
                    FILTER(cfn:function(?s)) . 
                } 
                """, StandardCharsets.UTF_8.toString());
        ResponseEntity<String> result = this.restTemplate.getForEntity(
                "http://localhost:%d/sparql/927294f7-0a9f-3d01-8120-b3ca3a45df38?token=XXX&query=%s".formatted(port, uriEscapedQueryString), 
                String.class);
        
        assertEquals(403, result.getStatusCode().value());
    }

    @Test
    public void shouldNotExecuteSparqlQueryWithAggregateFunction() throws Exception {

        String uriEscapedQueryString = URLEncoder.encode(
                """
                SELECT (<http://rdf4j.org/aggregate#stdev>(?o) AS ?stdev) WHERE { 
                    ?s ?p ?o .
                }
                """, StandardCharsets.UTF_8.toString());
        ResponseEntity<String> result = this.restTemplate.getForEntity(
                "http://localhost:%d/sparql/927294f7-0a9f-3d01-8120-b3ca3a45df38?token=XXX&query=%s".formatted(port, uriEscapedQueryString), 
                String.class);
        
        assertEquals(403, result.getStatusCode().value());
    }

    @Test
    public void shouldNotExecuteAskSparqlQuery() throws Exception {

        String uriEscapedQueryString = URLEncoder.encode(
                """
                ASK { 
                    ?s a <https://dalstonsemantics.com/ns/com/atlassian/page> . 
                } 
                """, StandardCharsets.UTF_8.toString());
        ResponseEntity<String> result = this.restTemplate.getForEntity(
                "http://localhost:%d/sparql/927294f7-0a9f-3d01-8120-b3ca3a45df38?token=XXX&query=%s".formatted(port, uriEscapedQueryString), 
                String.class);
        
        assertEquals(403, result.getStatusCode().value());
    }

    @Test
    public void shouldNotExecuteConstructSparqlQuery() throws Exception {

        String uriEscapedQueryString = URLEncoder.encode(
                """
                PREFIX cfn: <http://example.org/custom-function/> 
                CONSTRUCT {
                    ?s ?p ?o .
                }
                WHERE { 
                    ?s ?p ?o . 
                } 
                """, StandardCharsets.UTF_8.toString());
        ResponseEntity<String> result = this.restTemplate.getForEntity(
                "http://localhost:%d/sparql/927294f7-0a9f-3d01-8120-b3ca3a45df38?token=XXX&query=%s".formatted(port, uriEscapedQueryString), 
                String.class);
        
        assertEquals(403, result.getStatusCode().value());
    }

    @Test
    public void shouldNotExecuteSparqlUpdateOnSparqleQueryEndpoint() throws Exception {
        
        String uriEscapedQueryString = URLEncoder.encode(
                """
                DELETE { 
                    ?s ?p ?o .
                } 
                WHERE { 
                    ?s ?p ?o .
                } 
                """, StandardCharsets.UTF_8.toString());
        ResponseEntity<String> result = this.restTemplate.getForEntity(
                "http://localhost:%d/sparql/927294f7-0a9f-3d01-8120-b3ca3a45df38?token=XXX&query=%s".formatted(port, uriEscapedQueryString), 
                String.class);
        
        assertEquals(400, result.getStatusCode().value());
    }

    @Test
    public void shouldExecuteUrlEncodedPostSparqlQuery() throws IOException {

        String uriEscapedQueryString = URLEncoder.encode(
                """
                SELECT * 
                WHERE { 
                    ?s a <https://dalstonsemantics.com/ns/com/atlassian/page> . 
                }
                """, StandardCharsets.UTF_8.toString());
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        HttpEntity<String> request = new HttpEntity<String>("query=%s".formatted(uriEscapedQueryString), headers);

        ResponseEntity<String> result = this.restTemplate.postForEntity(
                "http://localhost:%d/sparql/927294f7-0a9f-3d01-8120-b3ca3a45df38?token=XXX".formatted(port),
                request, String.class);

        assertEquals(200, result.getStatusCode().value());
        assertEquals("application/sparql-results+json", result.getHeaders().getContentType().toString());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SparqlQueryOk.txt")), result.getBody());
    } 

    @Test
    public void shouldExecuteDirectPostSparqlQuery() throws IOException {

        String queryString =
                """
                SELECT * 
                WHERE { 
                    ?s a <https://dalstonsemantics.com/ns/com/atlassian/page> . 
                }
                """;
        
        HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/sparql-query");

        HttpEntity<String> request = new HttpEntity<String>(queryString, headers);

        ResponseEntity<String> result = this.restTemplate.postForEntity(
                "http://localhost:%d/sparql/927294f7-0a9f-3d01-8120-b3ca3a45df38?token=XXX".formatted(port),
                request, String.class);
        
        assertEquals(200, result.getStatusCode().value());
        assertEquals("application/sparql-results+json", result.getHeaders().getContentType().toString());
        assertEquals(Files.readString(Paths.get("./src/test/resources/responses/SparqlQueryOk.txt")), result.getBody());
    }
   
}