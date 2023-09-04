package com.dalstonsemantics.confluence.semantics.cloud;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
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

import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.resolver.IssParam;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ResourceMacroController {

    private static final String ACTIVE = "active";
  
    private Rdf4jRepositoryPool taxonomyRepositoryPool;
    private String baseUrl;
    private boolean enforceLicense;

    public ResourceMacroController(
            @Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool, 
            @Value("${addon.base-url}") String baseUrl,
            @Value("${addon.license.enforce}") boolean enforceLicense) {
        this.taxonomyRepositoryPool = taxonomyRepositoryPool;
        this.baseUrl = baseUrl;
        this.enforceLicense = enforceLicense;
    }

    @GetMapping(value = "/resource")
    public String getResourceMacro(@IssParam String iss, @RequestParam String lic, @RequestParam String uri, Model model, HttpServletResponse response) {

        log.info("Rendering resource. Uri: {}", uri);

        if (enforceLicense && !ACTIVE.equals(lic)) {
            throw new NoActiveLicenseException();
        }

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(iss);

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            ValueFactory vf = taxonomyRepository.getValueFactory();

            IRI taxonomyVersionGraph = vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss);
            IRI resource = vf.createIRI(uri);

            TupleQuery selectConcept = SPARQLFactory.tupleQueryWithConnection(
                    connection, 
                    """
                    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

                    SELECT ?type ?label ?pref_label
                    WHERE {
                        GRAPH ?taxonomyGraph {
                            OPTIONAL { 
                                FILTER(LANG(?label) = \"\" || LANGMATCHES(LANG(?label), \"en\"))
                                ?resource rdfs:label ?label .
                            }
                            OPTIONAL { 
                                FILTER(LANG(?pref_label) = \"\" || LANGMATCHES(LANG(?pref_label), \"en\"))
                                ?resource skos:prefLabel ?pref_label . 
                            }
                            ?resource a ?type .
                        }
                        GRAPH ?taxonomyVersionGraph {
                            ?taxonomyGraph a team:TaxonomyGraph ;
                                team:status ?taxonomyGraphStatus .
                        }
                    }
                    """);
            selectConcept.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
            selectConcept.setBinding("taxonomyGraphStatus", TEAM.CURRENT);
            selectConcept.setBinding("resource", resource);

            connection.setIsolationLevel(IsolationLevels.NONE);

            try (TupleQueryResult result = selectConcept.evaluate()) {
                if (result.hasNext()) {

                    BindingSet bindingSet = result.next();
                    Binding type = bindingSet.getBinding("type");
                    Binding label = bindingSet.getBinding("label");
                    Binding prefLabel = bindingSet.getBinding("pref_label");

                    if (type != null) {
                        model.addAttribute("type", type.getValue().stringValue());
                    }

                    if (label != null) {
                        model.addAttribute("label", label.getValue().stringValue());
                    }

                    if (prefLabel != null) {
                        model.addAttribute("prefLabel", prefLabel.getValue().stringValue());
                    }
                }
            }

            model.addAttribute("baseUrl", baseUrl);
        }

        response.addHeader("Cache-Control", "max-age=3600, must-revalidate, no-transform");

        return "resource-macro";
    }

    @GetMapping(value = "/resource-editor")
    public String getResourceMacroEditor(@RequestParam String lic) {

        if (enforceLicense && !ACTIVE.equals(lic)) {
            throw new NoActiveLicenseException();
        }

        return "resource-macro-editor";
    }
}