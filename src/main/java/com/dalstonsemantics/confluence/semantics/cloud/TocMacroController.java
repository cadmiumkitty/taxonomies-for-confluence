package com.dalstonsemantics.confluence.semantics.cloud;

import java.util.LinkedList;
import java.util.List;

import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.resolver.IssParam;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class TocMacroController {

    private static final String ACTIVE = "active";
    private static final int MAX_CONCEPTS = 5;

    private Rdf4jRepositoryPool taxonomyRepositoryPool;
    private boolean enforceLicense;

    public TocMacroController(@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool, 
            @Value("${addon.license.enforce}") boolean enforceLicense) {
        this.taxonomyRepositoryPool = taxonomyRepositoryPool;
        this.enforceLicense = enforceLicense;
    }

    @GetMapping(value = "/toc")
    public String getTocMacro(@IssParam String iss, @RequestParam String lic, @RequestParam List<Boolean> transitive, @RequestParam List<String> predicate, @RequestParam List<String> object, Model model) {

        log.info("Rendering TOC. Transitive: {}. Predicate: {}. Object: {}", transitive, predicate, object);

        if (enforceLicense && !ACTIVE.equals(lic)) {
            throw new NoActiveLicenseException();
        }

        int numCriteria = Math.min(transitive.size(), MAX_CONCEPTS);

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(iss);

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            ValueFactory vf = taxonomyRepository.getValueFactory();

            IRI taxonomyVersionGraph = vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss);
            IRI contentGraph = vf.createIRI(Namespaces.CONTENT_GRAPH, iss);

            // This is a kid's way of building the query but I don't want to deal with the 
            // uncertainty of Rdf4j SPARQL Query Builder for now
            // For now force MAX_CONCEPTS concepts including Subject
            StringBuilder selectContentBuilder = new StringBuilder();
            selectContentBuilder.append("PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/> ");
            selectContentBuilder.append("PREFIX dcterms: <http://purl.org/dc/terms/> ");
            selectContentBuilder.append("PREFIX skos: <http://www.w3.org/2004/02/skos/core#> ");
            selectContentBuilder.append("SELECT DISTINCT ?contentId ");
            selectContentBuilder.append("WHERE { ");

            selectContentBuilder.append("GRAPH ?contentGraph { ");
            for (int i = 0; i < numCriteria; i++) {
                selectContentBuilder.append(String.format("?statement%d a rdf:Statement ; ", i));
                selectContentBuilder.append("rdf:subject ?contentS ; ");
                selectContentBuilder.append(String.format("rdf:predicate ?predicate%d ; ", i));
                if (transitive.get(i)) {
                    selectContentBuilder.append(String.format("rdf:object ?concept%d . ", i));
                } else {
                    selectContentBuilder.append(String.format("rdf:object ?object%d . ", i));
                }
            }
            selectContentBuilder.append("?contentS team:contentId ?contentId ; ");
            selectContentBuilder.append("team:status ?status . ");
            selectContentBuilder.append("} ");

            selectContentBuilder.append("GRAPH ?taxonomyGraph { ");
            for (int i = 0; i < numCriteria; i++) {
                if (transitive.get(i)) {
                    selectContentBuilder.append(String.format("?concept%d skos:broader* ?object%d . ", i, i));
                }
            }
            selectContentBuilder.append("} ");

            selectContentBuilder.append("GRAPH ?taxonomyVersionGraph { ");
            selectContentBuilder.append("?taxonomyGraph a team:TaxonomyGraph ; ");
            selectContentBuilder.append("team:status ?taxonomyGraphStatus . ");
            selectContentBuilder.append("} ");            

            selectContentBuilder.append("} ");

            String selectContentQueryString = selectContentBuilder.toString();

            TupleQuery selectContent = connection.prepareTupleQuery(QueryLanguage.SPARQL, selectContentQueryString);
            selectContent.setIncludeInferred(false);
            selectContent.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
            selectContent.setBinding("taxonomyGraphStatus", TEAM.CURRENT);
            selectContent.setBinding("contentGraph", contentGraph);
            selectContent.setBinding("status", TEAM.CURRENT);
            for (int i = 0; i < numCriteria; i++) {
                selectContent.setBinding(String.format("predicate%d", i), vf.createIRI(predicate.get(i)));
                selectContent.setBinding(String.format("object%d", i), vf.createIRI(object.get(i)));
            }

            List<String> content = new LinkedList<>();
            connection.setIsolationLevel(IsolationLevels.NONE);

            try (TupleQueryResult result = selectContent.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();
                    String contentId = bindingSet.getBinding("contentId").getValue().stringValue();
                    content.add(contentId);
                }
            }

            model.addAttribute("content", content);
        }

        return "toc-macro";
    }

    @GetMapping(value = "/toc-editor")
    public String getTocMacroEditor(@RequestParam String lic) {

        if (enforceLicense && !ACTIVE.equals(lic)) {
            throw new NoActiveLicenseException();
        }

        return "toc-macro-editor";
    }
}