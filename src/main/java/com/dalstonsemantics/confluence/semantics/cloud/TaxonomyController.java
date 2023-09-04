package com.dalstonsemantics.confluence.semantics.cloud;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.ContextJwt;
import com.dalstonsemantics.confluence.semantics.cloud.domain.addon.AddOn;
import com.dalstonsemantics.confluence.semantics.cloud.domain.addon.License;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.resolver.IssParam;
import com.dalstonsemantics.confluence.semantics.cloud.service.AddOnService;
import com.dalstonsemantics.confluence.semantics.cloud.service.ContentService;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.JSONLDMode;
import org.eclipse.rdf4j.rio.helpers.JSONLDSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class TaxonomyController {

    private static final String ACTIVE = "active";
  
    private Rdf4jRepositoryPool taxonomyRepositoryPool;

    private AtlassianHostRepository atlassianHostRepository;
    private AddOnService addOnService;
    private ProvenanceModelEnricher provenanceModelEnricher;
    private boolean enforceLicense;

    public TaxonomyController(@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool, 
            @Autowired AtlassianHostRepository atlassianHostRepository, 
            @Autowired ContentService contentService, 
            @Autowired AddOnService addOnService,
            @Autowired ProvenanceModelEnricher provenanceModelEnricher,
            @Value("${addon.license.enforce}") boolean enforceLicense) {
        this.taxonomyRepositoryPool = taxonomyRepositoryPool;
        this.atlassianHostRepository = atlassianHostRepository;
        this.addOnService = addOnService;
        this.provenanceModelEnricher = provenanceModelEnricher;
        this.enforceLicense = enforceLicense;
    }

    @GetMapping(value = "/subject-byline")
    public String getSubjectByline(@RequestParam String lic) {

        if (enforceLicense && !ACTIVE.equals(lic)) {
            throw new NoActiveLicenseException();
        }

        return "subject-byline";
    }

    @GetMapping(value = "/type-byline")
    public String getTypeByline(@RequestParam String lic) {

        if (enforceLicense && !ACTIVE.equals(lic)) {
            throw new NoActiveLicenseException();
        }

        return "type-byline";
    }    

    @GetMapping(value = "/taxonomy-admin-page")
    public String getTaxonomyAdmin(@RequestParam String lic) {

        if (enforceLicense && !ACTIVE.equals(lic)) {
            throw new NoActiveLicenseException();
        }

        return "taxonomy-admin-page";
    }

    @GetMapping(value = "/taxonomy-page")
    public String getTaxonomyPage(@RequestParam String lic) {

        if (enforceLicense && !ACTIVE.equals(lic)) {
            throw new NoActiveLicenseException();
        }

        return "taxonomy-page";
    }

    @ContextJwt
    @GetMapping(value = "/taxonomy/conceptscheme", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getTaxonomyConceptSchemes(@IssParam String iss, @RequestParam(required = false) String context, OutputStream os) {

        log.info("Retrieving taxonomy. Iss: {}", iss);

        AtlassianHost host = atlassianHostRepository.findById(iss).get();

        AddOn addOn = addOnService.getAddOn(host);
        License lic = addOn.getLicense();

        log.debug("Checking license. AddOn: {}. License: {}. Host: {}", addOn, lic, host);

        if (enforceLicense && lic != null && !lic.getActive()) {
            throw new NoActiveLicenseException();
        }

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(iss);

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            ValueFactory vf = taxonomyRepository.getValueFactory();

            RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, os);
            writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.FLATTEN);

            GraphQuery construct = SPARQLFactory.graphQueryWithConnection(
                    connection, 
                    """
                    PREFIX dcterms: <http://purl.org/dc/terms/>
                    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    CONSTRUCT {
                        ?s ?p ?o .
                        ?s team:conceptCount ?conceptCount .
                        ?s team:statementCount ?statementCount .
                    }
                    WHERE {
                        {
                            SELECT ?s ?p ?o (COUNT(?concept) AS ?conceptCount)
                            WHERE {
                                GRAPH ?taxonomyGraph {
                                    OPTIONAL { ?concept skos:inScheme ?s . }
                                    ?s ?p ?o .
                                    ?s a skos:ConceptScheme .
                                }
                                %s
                            } GROUP BY ?s ?p ?o
                        }
                        UNION
                        {
                            SELECT ?s (COUNT(?statement) AS ?statementCount)
                            WHERE {
                                GRAPH ?contentGraph { 
                                    ?statement a rdf:Statement ; 
                                        rdf:subject ?content ; 
                                        rdf:object ?concept ; 
                                        team:status ?status . 
                                    ?content team:status ?status . 
                                }
                                GRAPH ?taxonomyGraph {
                                    ?concept skos:inScheme ?s .
                                    ?s a skos:ConceptScheme .
                                }
                                %s
                            } GROUP BY ?s
                        }
                    }
                    """,
                    context == null ? "GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } " : "",
                    context == null ? "GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } " : "");

            if (context == null) {
                construct.setBinding("taxonomyVersionGraph", vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss));
                construct.setBinding("taxonomyGraphStatus", TEAM.CURRENT);
            } else {
                construct.setBinding("taxonomyGraph", vf.createIRI(context));
            }
            construct.setBinding("contentGraph", vf.createIRI(Namespaces.CONTENT_GRAPH, iss));
            construct.setBinding("status", TEAM.CURRENT);
                                
            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }

    @ContextJwt
    @GetMapping(value = "/taxonomy/topconcept", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getTaxonomyTopConcepts(@IssParam String iss, @RequestParam String scheme, @RequestParam(required = false) String context, OutputStream os) {

        log.info("Retrieving taxonomy. Iss: {}", iss);

        AtlassianHost host = atlassianHostRepository.findById(iss).get();

        AddOn addOn = addOnService.getAddOn(host);
        License lic = addOn.getLicense();

        log.debug("Checking license. AddOn: {}. License: {}. Host: {}", addOn, lic, host);

        if (enforceLicense && lic != null && !lic.getActive()) {
            throw new NoActiveLicenseException();
        }

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(iss);

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            ValueFactory vf = taxonomyRepository.getValueFactory();

            RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, os);
            writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.FLATTEN);

            GraphQuery construct = SPARQLFactory.graphQueryWithConnection(
                    connection,
                    """
                    PREFIX dcterms: <http://purl.org/dc/terms/>
                    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    CONSTRUCT {
                        ?s ?p ?o .
                        ?s team:conceptCount ?conceptCount .
                        ?s team:statementCount ?statementCount .
                    }
                    WHERE {
                        GRAPH ?taxonomyGraph {
                            ?s ?p ?o .
                            {
                                SELECT ?s
                                WHERE {
                                    {
                                        ?s skos:topConceptOf ?cs .
                                    }
                                    UNION
                                    {
                                        ?s skos:inScheme ?cs .
                                        FILTER NOT EXISTS { ?s skos:broader ?y . }
                                    }
                                }
                            }
                            {
                                SELECT ?s (COUNT(?narrower) AS ?conceptCount)
                                WHERE {
                                    ?narrower skos:broader* ?s .
                                } GROUP BY ?s
                            }
                            UNION
                            {
                                SELECT ?s (COUNT(?statement) AS ?statementCount)
                                WHERE {
                                    GRAPH ?contentGraph { 
                                        ?statement a rdf:Statement ; 
                                            rdf:subject ?content ; 
                                            rdf:object ?narrower ; 
                                            team:status ?status . 
                                        ?content team:status ?status . 
                                    }
                                    ?narrower skos:broader* ?s . 
                                } GROUP BY ?s
                            }
                        }
                        %s
                    }
                    """,
                    context == null 
                        ? "GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } " 
                        : "GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph . FILTER ( ?taxonomyGraph = ?context ) }");

            // Reason to introduce context as opposed to direcetly binding is an apparent query optimizer issue.
            if (context == null) {
                construct.setBinding("taxonomyVersionGraph", vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss));
                construct.setBinding("taxonomyGraphStatus", TEAM.CURRENT);
            } else {
                construct.setBinding("taxonomyVersionGraph", vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss));
                construct.setBinding("context", vf.createIRI(context));
            }
            construct.setBinding("contentGraph", vf.createIRI(Namespaces.CONTENT_GRAPH, iss));
            construct.setBinding("status", TEAM.CURRENT);
            construct.setBinding("cs", vf.createIRI(scheme));
            
            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }

    @ContextJwt
    @GetMapping(value = "/taxonomy/narrowerconcept", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getTaxonomyNarrowerConcepts(@IssParam String iss, @RequestParam String broader, @RequestParam(required = false) String context, OutputStream os) {

        log.info("Retrieving taxonomy. Iss: {}", iss);

        AtlassianHost host = atlassianHostRepository.findById(iss).get();

        AddOn addOn = addOnService.getAddOn(host);
        License lic = addOn.getLicense();

        log.debug("Checking license. AddOn: {}. License: {}. Host: {}", addOn, lic, host);

        if (enforceLicense && lic != null && !lic.getActive()) {
            throw new NoActiveLicenseException();
        }

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(iss);

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            ValueFactory vf = taxonomyRepository.getValueFactory();

            RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, os);
            writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.FLATTEN);

            GraphQuery construct = SPARQLFactory.graphQueryWithConnection(
                    connection,
                    """
                    PREFIX dcterms: <http://purl.org/dc/terms/>
                    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    CONSTRUCT {
                        ?s ?p ?o .
                        ?s team:conceptCount ?conceptCount .
                        ?s team:statementCount ?statementCount .
                    }
                    WHERE {
                        GRAPH ?taxonomyGraph {
                            {
                                SELECT ?s (COUNT(?narrower) AS ?conceptCount)
                                WHERE {
                                    ?narrower skos:broader* ?s .
                                } GROUP BY ?s
                            }
                            UNION
                            {
                                SELECT ?s (COUNT(?statement) AS ?statementCount)
                                WHERE {
                                    GRAPH ?contentGraph { 
                                        ?statement a rdf:Statement ; 
                                            rdf:subject ?content ; 
                                            rdf:object ?narrower ; 
                                            team:status ?status . 
                                        ?content team:status ?status . 
                                    }
                                    ?narrower skos:broader* ?s . 
                                } GROUP BY ?s
                            }
                            ?s ?p ?o .
                            ?s skos:broader ?b .
                        }
                        %s
                    }
                    """,
                    context == null 
                        ? "GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } " 
                        : "GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph . FILTER ( ?taxonomyGraph = ?context ) }");

            if (context == null) {
                construct.setBinding("taxonomyVersionGraph", vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss));
                construct.setBinding("taxonomyGraphStatus", TEAM.CURRENT);
            } else {
                construct.setBinding("taxonomyVersionGraph", vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss));
                construct.setBinding("context", vf.createIRI(context));
            }
            construct.setBinding("contentGraph", vf.createIRI(Namespaces.CONTENT_GRAPH, iss));
            construct.setBinding("status", TEAM.CURRENT);
            construct.setBinding("b", vf.createIRI(broader));

            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }

    @ContextJwt
    @GetMapping(value = "/taxonomy/concept", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getTaxonomyConcept(@IssParam String iss, @RequestParam String uri, @RequestParam(required = false) String context, OutputStream os) throws IOException {

        log.info("Retrieving concept. Iss: {}. Concept: {}", iss, uri);

        AtlassianHost host = atlassianHostRepository.findById(iss).get();

        AddOn addOn = addOnService.getAddOn(host);
        License lic = addOn.getLicense();

        log.debug("Checking license. AddOn: {}. License: {}. Host: {}", addOn, lic, host);

        if (enforceLicense && lic != null && !lic.getActive()) {
            throw new NoActiveLicenseException();
        }

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(iss);

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            ValueFactory vf = taxonomyRepository.getValueFactory();

            RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, os);
            writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.FLATTEN);

            GraphQuery construct = SPARQLFactory.graphQueryWithConnection(
                    connection,
                    """
                    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    CONSTRUCT {
                        ?s ?p ?o .
                        ?scheme a skos:ConceptScheme .
                        ?scheme skos:prefLabel ?scheme_skos_pref_label .
                        ?scheme rdfs:label ?scheme_rdfs_label .
                    }
                    WHERE {
                        GRAPH ?taxonomyGraph {
                            ?s ?p ?o .
                            OPTIONAL {
                                ?s skos:inScheme ?scheme .
                                ?scheme a skos:ConceptScheme .
                                OPTIONAL { ?scheme skos:prefLabel ?scheme_skos_pref_label . }
                                OPTIONAL { ?scheme rdfs:label ?scheme_rdfs_label . }
                            }
                        }
                        %s
                    }
                    """,
                    context == null ? "GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } " : "");

            if (context == null) {
                construct.setBinding("taxonomyVersionGraph", vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss));
                construct.setBinding("taxonomyGraphStatus", TEAM.CURRENT);
            } else {
                construct.setBinding("taxonomyGraph", vf.createIRI(context));
            }
            construct.setBinding("contentGraph", vf.createIRI(Namespaces.CONTENT_GRAPH, iss));
            construct.setBinding("status", TEAM.CURRENT);
            construct.setBinding("s", vf.createIRI(uri));
            
            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }

    @ContextJwt
    @GetMapping(value = "/taxonomy/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void queryTaxonomyConcepts(@IssParam String iss, @RequestParam(name = "q") String query, OutputStream os) throws IOException {

        log.info("Retrieving concepts. Iss: {}. Search term: {}", iss, query);

        AtlassianHost host = atlassianHostRepository.findById(iss).get();

        AddOn addOn = addOnService.getAddOn(host);
        License lic = addOn.getLicense();

        log.debug("Checking license. AddOn: {}. License: {}. Host: {}", addOn, lic, host);

        if (enforceLicense && lic != null && !lic.getActive()) {
            throw new NoActiveLicenseException();
        }

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(iss);

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            ValueFactory vf = taxonomyRepository.getValueFactory();

            Literal q = vf.createLiteral(query);

            RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, os);
            writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.FLATTEN);

            GraphQuery construct = SPARQLFactory.graphQueryWithConnection(
                    connection,
                    """
                    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                    CONSTRUCT {
                        ?s ?p ?o .
                        ?s team:searchScore ?searchScore .
                        ?scheme a skos:ConceptScheme .
                        ?scheme skos:prefLabel ?scheme_skos_pref_label .
                        ?scheme rdfs:label ?scheme_rdfs_label .
                    }
                    WHERE {
                        GRAPH ?taxonomyGraph {
                            ?s ?p ?o .
                            OPTIONAL {
                                ?s skos:inScheme ?scheme .
                                ?scheme a skos:ConceptScheme .
                                OPTIONAL { ?scheme skos:prefLabel ?scheme_skos_pref_label . }
                                OPTIONAL { ?scheme rdfs:label ?scheme_rdfs_label . }
                            }
                            { 
                                SELECT DISTINCT ?s ?searchScore {
                                    ?s a skos:Concept ;
                                        skos:prefLabel ?prefLabel .
                                    OPTIONAL { ?s skos:altLabel ?altLabel }
                                    OPTIONAL { ?s skos:notation ?notation }
                                    BIND (LCASE(CONCAT(?prefLabel, IF(BOUND(?altLabel), ?altLabel, ""), IF(BOUND(?notation), ?notation, ""))) AS ?fullTextLowerCase)
                                    BIND (xsd:float(STRLEN(?q) / STRLEN(?fullTextLowerCase)) AS ?searchScore)
                                    FILTER (CONTAINS(?fullTextLowerCase,LCASE(?q)))
                                } ORDER BY DESC(?searchScore) LIMIT 10
                            }
                        }
                        GRAPH ?taxonomyVersionGraph { 
                            ?taxonomyGraph a team:TaxonomyGraph ; 
                                team:status ?taxonomyGraphStatus . 
                        } 
                    }
                    """);
            construct.setBinding("taxonomyVersionGraph", vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss));
            construct.setBinding("taxonomyGraphStatus", TEAM.CURRENT);
            construct.setBinding("q", q);
            
            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }

    /**
     * Returns graph of content where skos:Concept is an Object, e.g. content that is dcterms:related to skos:Concept.
     * 
     * @param iss Value of the iss param in the JWT Claim.
     * @param concept skos:Concept to retrieve conten.
     * @param os Stream to write the response to.
     */
    @ContextJwt
    @GetMapping(value = "/taxonomy/content", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getTaxonomyContent(@IssParam String iss, @RequestParam String concept, OutputStream os) {

        log.info("Retrieving content for subject. Iss: {}. Concept: {}", iss, concept);

        AtlassianHost host = atlassianHostRepository.findById(iss).get();

        AddOn addOn = addOnService.getAddOn(host);
        License lic = addOn.getLicense();

        log.debug("Checking license. AddOn: {}. License: {}. Host: {}", addOn, lic, host);

        if (enforceLicense && lic != null && !lic.getActive()) {
            throw new NoActiveLicenseException();
        }

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(iss);

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            ValueFactory vf = taxonomyRepository.getValueFactory();

            RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, os);
            writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.FLATTEN);

            GraphQuery construct = SPARQLFactory.graphQueryWithConnection(
                    connection,
                    """
                    PREFIX dcterms: <http://purl.org/dc/terms/>
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    CONSTRUCT {
                        ?contentS ?contentP ?contentO ;
                            ?predicateS ?conceptS .
                    }
                    WHERE {
                        GRAPH ?contentGraph {
                            ?statementS a rdf:Statement ;
                                rdf:subject ?contentS ;
                                rdf:predicate ?predicateS ;
                                rdf:object ?conceptS ;
                                team:status ?status .
                            ?contentS a ?contentClass ;
                                team:status ?status ;
                                ?contentP ?contentO .
                        }
                    } 
                    """);
            construct.setBinding("contentGraph", vf.createIRI(Namespaces.CONTENT_GRAPH, iss));
            construct.setBinding("conceptS",  vf.createIRI(concept));
            construct.setBinding("status", TEAM.CURRENT);
            
            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }    

    @ContextJwt
    @GetMapping(value = "/provenance", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getProvenance(@IssParam String iss, @RequestParam String resource, OutputStream os) {

        log.info("Retrieving provenance. Iss: {}. Resource: {}", iss, resource);

        AtlassianHost host = atlassianHostRepository.findById(iss).get();

        AddOn addOn = addOnService.getAddOn(host);
        License lic = addOn.getLicense();

        log.debug("Checking license. AddOn: {}. License: {}. Host: {}", addOn, lic, host);

        if (enforceLicense && lic != null && !lic.getActive()) {
            throw new NoActiveLicenseException();
        }

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(iss);

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            ValueFactory vf = taxonomyRepository.getValueFactory();

            RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, os);
            writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.FLATTEN);

            GraphQuery construct = SPARQLFactory.graphQueryWithConnection(
                    connection,
                    """
                    PREFIX dcterms: <http://purl.org/dc/terms/>
                    PREFIX prov: <http://www.w3.org/ns/prov#>
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    CONSTRUCT {
                        ?activity a prov:Activity ;
                            prov:wasAssociatedWith ?agent ;
                            prov:used ?value ;
                            prov:startedAtTime ?startedAtTime ;
                            prov:endedAtTime ?endeddAtTime .
                        ?agent ?ap ?ao .
                        ?value ?vp ?vo .
                        ?concept ?cp ?co .
                    }
                    WHERE {
                        {
                            GRAPH ?taxonomyVersionGraph {
                                ?activity a prov:Activity ;
                                    prov:wasAssociatedWith ?agent ;
                                    prov:generated ?resource ;
                                    prov:startedAtTime ?startedAtTime ;
                                    prov:endedAtTime ?endeddAtTime .
                                ?agent ?ap ?ao .
                            }
                        }
                        UNION
                        {
                            GRAPH ?contentGraph {
                                ?statement a rdf:Statement ;
                                    rdf:subject ?resource .
                                ?activity a prov:Activity ;
                                    prov:wasAssociatedWith ?agent ;
                                    prov:generated ?statement ;
                                    prov:used ?value ;
                                    prov:startedAtTime ?startedAtTime ;
                                    prov:endedAtTime ?endeddAtTime .
                                ?agent ?ap ?ao .
                                ?value ?any ?concept ;
                                    ?vp ?vo .
                            }
                            GRAPH ?taxonomyGraph {
                                ?concept ?cp ?co .
                            }
                            GRAPH ?taxonomyVersionGraph { 
                                ?taxonomyGraph a team:TaxonomyGraph ; 
                                    team:status ?taxonomyGraphStatus . 
                            }     
                        }
                    }
                    """);
            construct.setBinding("taxonomyVersionGraph", vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss));
            construct.setBinding("taxonomyGraphStatus", TEAM.CURRENT);
            construct.setBinding("contentGraph", vf.createIRI(Namespaces.CONTENT_GRAPH, iss));
            construct.setBinding("resource", vf.createIRI(resource));
            
            connection.setIsolationLevel(IsolationLevels.NONE);

            try (GraphQueryResult result = construct.evaluate()) {

                Model model = QueryResults.asModel(result);

                List<Statement> statements = provenanceModelEnricher.enrichProvenanceModel(host, connection, model);
                
                model.addAll(statements);

                writer.startRDF();
                for (Statement statement: model) {
                  writer.handleStatement(statement);
                }
                writer.endRDF();
            }
        }
    }
}