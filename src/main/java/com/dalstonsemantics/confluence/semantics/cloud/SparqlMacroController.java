package com.dalstonsemantics.confluence.semantics.cloud;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.query.parser.ParsedBooleanQuery;
import org.eclipse.rdf4j.query.parser.ParsedGraphQuery;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.ParsedTupleQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.resolver.IssParam;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class SparqlMacroController {

    private static final String ACTIVE = "active";

    private Rdf4jRepositoryPool taxonomyRepositoryPool;
    private boolean enforceLicense;
    private int maxExecutionTime;
    private int maxRows;

    public SparqlMacroController (@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool, 
            @Value("${addon.license.enforce}") boolean enforceLicense,
            @Value("${addon.sparql-macro.max-execution-time-sec}") int maxExecutionTime,
            @Value("${addon.sparql-macro.max-rows}") int maxRows) {
        this.taxonomyRepositoryPool = taxonomyRepositoryPool;
        this.enforceLicense = enforceLicense;
        this.maxExecutionTime = maxExecutionTime;
        this.maxRows = maxRows;
    }

    @GetMapping(value = "/sparql-macro")
    public String getSparqlMacro(@IssParam String iss, @RequestParam String lic, @RequestParam(required = false) String pageId, @RequestParam(required = false) String pageType, @RequestParam String q, Model model) {

        log.info("Rendering SPARQL. Query: {}", q);

        if (enforceLicense && !ACTIVE.equals(lic)) {
            throw new NoActiveLicenseException();
        }

        String decodedQuery = decodeQuery(q);

        try {

            SPARQLParser parser = new SPARQLParser();
            ParsedQuery parsedQuery = parser.parseQuery(decodedQuery, null);

            parsedQuery.getTupleExpr().visit(new SparqlRecursiveQueryModelVisitor());

            Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(iss);
    
            try (RepositoryConnection connection = taxonomyRepository.getConnection()) {
    
                ValueFactory vf = taxonomyRepository.getValueFactory();
    
                SimpleDataset dataset = new SimpleDataset();
                dataset.addDefaultGraph(vf.createIRI(Namespaces.MATERIALIZED_CONTENT_GRAPH, iss));
                dataset.addNamedGraph(vf.createIRI(Namespaces.MATERIALIZED_CONTENT_GRAPH, iss));
    
                if (parsedQuery instanceof ParsedBooleanQuery) {

                    model.addAttribute("booleanQueryException", true);
                } else if (parsedQuery instanceof ParsedGraphQuery) {

                    model.addAttribute("graphQueryException", true);
                } else if (parsedQuery instanceof ParsedTupleQuery) {
    
                    TupleQuery tupleQuery = connection.prepareTupleQuery(parsedQuery.getSourceString());
                    tupleQuery.setIncludeInferred(false);
                    tupleQuery.setMaxExecutionTime(maxExecutionTime);
                    tupleQuery.setDataset(dataset);

                    if (pageId != null) {
                        IRI thisResource = vf.createIRI(Namespaces.CONTENT, String.format("%s-%s", iss, pageId));
                        tupleQuery.setBinding("this", thisResource);
                    }
    
                    try (TupleQueryResult result = tupleQuery.evaluate()) {

                        List<String> columns = result.getBindingNames();
                        List<Map<String, org.eclipse.rdf4j.model.Value>> rows = new LinkedList<>();
                        int rowCounter = 0;

                        while (result.hasNext() && rowCounter <= maxRows) {

                            BindingSet bindingSet = result.next();
                            Map<String, org.eclipse.rdf4j.model.Value> row = new HashMap<>();
                            for (String bindingName : columns) {
                                Binding binding = bindingSet.getBinding(bindingName);
                                if (binding != null) {
                                    org.eclipse.rdf4j.model.Value value = binding.getValue();
                                    row.put(bindingName, value);
                                } else {
                                    row.put(bindingName, vf.createLiteral(""));
                                }
                            }

                            rows.add(row);
                            rowCounter ++;
                        }

                        model.addAttribute("columns", columns);
                        model.addAttribute("rows", rows);
                    }
                }
            }
        } catch (MalformedQueryException mqe) {

            model.addAttribute("malformedQueryException", true);
        } catch (Throwable th) {

            model.addAttribute("throwable", true);
        }

        model.addAttribute("decodedQuery", decodedQuery);

        return "sparql-macro";
    }

    @GetMapping(value = "/sparql-macro-editor")
    public String getTocMacroEditor(@RequestParam String lic) {

        if (enforceLicense && !ACTIVE.equals(lic)) {
            throw new NoActiveLicenseException();
        }

        return "sparql-macro-editor";
    }

    @SneakyThrows
    private String decodeQuery(String q) {
        return URLDecoder.decode(q, StandardCharsets.UTF_8.toString());
    }

}