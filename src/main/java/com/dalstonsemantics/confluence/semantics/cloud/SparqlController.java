package com.dalstonsemantics.confluence.semantics.cloud;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import com.atlassian.connect.spring.IgnoreJwt;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.dalstonsemantics.confluence.semantics.cloud.provider.SecretClientProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.SneakyThrows;

@Controller
public class SparqlController {

    private Rdf4jRepositoryPool taxonomyRepositoryPool;
    private SecretClientProvider secretClientProvider;
    private String keyVaultUrl;
    private int maxExecutionTime;

    public SparqlController(@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool,
            @Autowired SecretClientProvider secretClientProvider,
            @Value("${addon.key-vault.url}") String keyVaultUrl,
            @Value("${addon.sparql.max-execution-time-sec}") int maxExecutionTime) {
        this.taxonomyRepositoryPool = taxonomyRepositoryPool;
        this.secretClientProvider = secretClientProvider;
        this.keyVaultUrl = keyVaultUrl;
        this.maxExecutionTime = maxExecutionTime;
    }

    @IgnoreJwt
    @GetMapping(value = "/sparql/{iss}", produces = {"application/sparql-results+json"})
    @ResponseStatus(value = HttpStatus.OK)
    public void getTaxonomyConceptSchemes(@PathVariable String iss, @RequestParam(required = true) String token, 
            @RequestParam(required = true) String query, HttpServletResponse response) {

        evaluateSparqlQuery(iss, token, query, response);
    }

    @IgnoreJwt
    @PostMapping(value = "/sparql/{iss}", consumes = {"application/x-www-form-urlencoded"}, produces = {"application/sparql-results+json"})
    @ResponseStatus(value = HttpStatus.OK)
    public void urlEncodedPostTaxonomyConceptSchemes(@PathVariable String iss, @RequestParam(required = true) String token, 
            @RequestParam(required = true) String query, HttpServletResponse response) {

        evaluateSparqlQuery(iss, token, query, response);
    }

    @IgnoreJwt
    @PostMapping(value = "/sparql/{iss}", consumes = {"application/sparql-query"}, produces = {"application/sparql-results+json"})
    @ResponseStatus(value = HttpStatus.OK)
    public void postTaxonomyConceptSchemes(@PathVariable String iss, @RequestParam(required = true) String token, 
            @RequestBody String query, HttpServletResponse response) {

        evaluateSparqlQuery(iss, token, query, response);
    }

    @SneakyThrows
    private void evaluateSparqlQuery(String iss, String token, String query, HttpServletResponse response) {

        KeyVaultSecret keyVaultSecret = secretClientProvider.getSecretClient(this.keyVaultUrl).getSecret("sparql-%s".formatted(iss));
        if (keyVaultSecret == null || (keyVaultSecret != null && !keyVaultSecret.getValue().equals(token))) {
            throw new SparqlAuthenticationFailedException();
        }

        String decodedQuery = URLDecoder.decode(query, StandardCharsets.UTF_8.toString());

        ParsedQuery parsedQuery;
        try {

            SPARQLParser parser = new SPARQLParser();
            parsedQuery = parser.parseQuery(decodedQuery, null);
        } catch (MalformedQueryException mqe) {

            throw new SparqlMalformedQueryException();
        }

        parsedQuery.getTupleExpr().visit(new SparqlRecursiveQueryModelVisitor());

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(iss);

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            ValueFactory vf = taxonomyRepository.getValueFactory();

            SimpleDataset dataset = new SimpleDataset();
            dataset.addDefaultGraph(vf.createIRI(Namespaces.MATERIALIZED_CONTENT_GRAPH, iss));
            dataset.addNamedGraph(vf.createIRI(Namespaces.MATERIALIZED_CONTENT_GRAPH, iss));

            if (parsedQuery instanceof ParsedBooleanQuery) {

                throw new SparqlForbiddenException();
            } else if (parsedQuery instanceof ParsedGraphQuery) {

                throw new SparqlForbiddenException();
            } else if (parsedQuery instanceof ParsedTupleQuery) {

                TupleQuery tupleQuery = connection.prepareTupleQuery(parsedQuery.getSourceString());
                tupleQuery.setIncludeInferred(false);
                tupleQuery.setMaxExecutionTime(maxExecutionTime);
                tupleQuery.setDataset(dataset);

                response.setContentType("application/sparql-results+json");
                tupleQuery.evaluate(new SPARQLResultsJSONWriter(response.getOutputStream()));
            }
        }
    }

}