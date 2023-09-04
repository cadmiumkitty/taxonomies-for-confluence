package com.dalstonsemantics.confluence.semantics.cloud;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.GraphQuery;
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

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.ContextJwt;
import com.dalstonsemantics.confluence.semantics.cloud.domain.addon.AddOn;
import com.dalstonsemantics.confluence.semantics.cloud.domain.addon.License;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.resolver.IssParam;
import com.dalstonsemantics.confluence.semantics.cloud.service.AddOnService;
import com.dalstonsemantics.confluence.semantics.cloud.service.ContentService;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class SchemaController {

    private static final String ACTIVE = "active";
  
    private Rdf4jRepositoryPool taxonomyRepositoryPool;

    private AtlassianHostRepository atlassianHostRepository;
    private AddOnService addOnService;
    private UUIDProvider uuidp;
    private boolean enforceLicense;

    public SchemaController(@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool, 
            @Autowired AtlassianHostRepository atlassianHostRepository, 
            @Autowired ContentService contentService, 
            @Autowired AddOnService addOnService,
            @Autowired UUIDProvider uuidp,
            @Value("${addon.license.enforce}") boolean enforceLicense) {
        this.taxonomyRepositoryPool = taxonomyRepositoryPool;
        this.atlassianHostRepository = atlassianHostRepository;
        this.addOnService = addOnService;
        this.uuidp = uuidp;
        this.enforceLicense = enforceLicense;
    }

    @ContextJwt
    @GetMapping(value = "/schema/resource", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSchemaResource(@IssParam String iss, @RequestParam String uri, @RequestParam(required = false) String context, OutputStream os) throws IOException {

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
                        ?defined_by rdfs:label ?defined_by_label .
                    }
                    WHERE {
                        GRAPH ?taxonomyGraph {
                            OPTIONAL {
                                OPTIONAL { ?scheme skos:prefLabel ?scheme_skos_pref_label . }
                                OPTIONAL { ?scheme rdfs:label ?scheme_rdfs_label . }
                                ?scheme a skos:ConceptScheme .
                                ?s skos:inScheme ?scheme .
                            }
                            OPTIONAL {
                                ?defined_by rdfs:label ?defined_by_label .
                                ?s rdfs:isDefinedBy ?defined_by .
                            }
                            ?s ?p ?o .
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
    @GetMapping(value = "/schema/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void querySchemaResources(@IssParam String iss, @RequestParam(name = "q") String query, OutputStream os) throws IOException {

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
                        ?defined_by rdfs:label ?defined_by_label .
                    }
                    WHERE {
                        GRAPH ?taxonomyGraph {
                            ?s ?p ?o .
                            OPTIONAL {
                                ?defined_by rdfs:label ?defined_by_label .
                                ?s rdfs:isDefinedBy ?defined_by .
                            }
                            OPTIONAL {
                                ?s skos:inScheme ?scheme .
                                ?scheme a skos:ConceptScheme .
                                OPTIONAL { ?scheme skos:prefLabel ?scheme_skos_pref_label . }
                                OPTIONAL { ?scheme rdfs:label ?scheme_rdfs_label . }
                            }
                            { 
                                SELECT DISTINCT ?s ?searchScore {
                                    ?s a ?s_class .
                                    FILTER ( ?s_class IN (skos:Concept, rdf:Property, rdfs:Class) )
                                    OPTIONAL { ?s rdfs:label ?label }
                                    OPTIONAL { ?s skos:prefLabel ?pref_label }
                                    OPTIONAL { ?s skos:altLabel ?alt_label }
                                    OPTIONAL { ?s skos:notation ?notation }
                                    BIND (
                                        LCASE(
                                            CONCAT(
                                                IF( BOUND( ?label ), ?label, "" ),
                                                IF( BOUND( ?pref_label ), ?pref_label, "" ), 
                                                IF( BOUND( ?alt_label ), ?alt_label, "" ), 
                                                IF( BOUND( ?notation ), ?notation, "" )
                                            )
                                        )
                                        AS ?fullTextLowerCase )
                                    BIND ( xsd:float( STRLEN( ?q ) / STRLEN( ?fullTextLowerCase ) ) AS ?searchScore )                                    
                                    FILTER ( CONTAINS ( ?fullTextLowerCase, LCASE( ?q ) ) )
                                } ORDER BY DESC( ?searchScore ) LIMIT 10
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
}