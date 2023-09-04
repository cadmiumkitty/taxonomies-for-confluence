package com.dalstonsemantics.confluence.semantics.cloud;

import java.io.OutputStream;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.BNode;
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
public class SchemaPropertyController {

    private static final String ACTIVE = "active";
  
    private Rdf4jRepositoryPool taxonomyRepositoryPool;

    private AtlassianHostRepository atlassianHostRepository;
    private AddOnService addOnService;
    private UUIDProvider uuidp;
    private boolean enforceLicense;

    public SchemaPropertyController(@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool, 
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
    @GetMapping(value = "/schema/defining-resources-for-properties", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSchemaDefiningResourcesForProperties(@IssParam String iss, @RequestParam(required = false) String context, OutputStream os) {

        log.info("Retrieving defining resources for properties. Iss: {}", iss);

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
                            ?propertyDefiningResource ?p ?o .
                            ?propertyDefiningResource team:propertyCount ?definingResourcePropertyCount .
                            ?propertyDefiningResource team:statementCount ?definingResourceStatementCount .
                            ?propertyDefiningResource a team:DefiningResource .
                            ?blank rdfs:label "Properties with no defining resources" .
                            ?blank team:propertyCount ?propertyCount .
                            ?blank team:statementCount ?statementCount .
                            ?blank a team:BlankDefiningResource .
                        }
                        WHERE {
                            {
                                {
                                    SELECT ?propertyDefiningResource ?p ?o (COUNT(DISTINCT(?property)) AS ?definingResourcePropertyCount)
                                    WHERE {
                                        GRAPH ?taxonomyGraph {
                                            ?propertyDefiningResource ?p ?o .
                                            ?property a rdf:Property .
                                            ?property rdfs:isDefinedBy ?propertyDefiningResource .
                                        }
                                        %s
                                    } GROUP BY ?propertyDefiningResource ?p ?o
                                }
                                UNION
                                {
                                    SELECT ?propertyDefiningResource (COUNT(DISTINCT(?statement)) AS ?definingResourceStatementCount)
                                    WHERE {
                                        GRAPH ?contentGraph { 
                                            ?statement a rdf:Statement ; 
                                                rdf:subject ?content ; 
                                                rdf:predicate ?property ; 
                                                team:status ?status . 
                                            ?content team:status ?status . 
                                        }
                                        GRAPH ?taxonomyGraph {
                                            ?property a rdf:Property .
                                            ?property rdfs:isDefinedBy ?propertyDefiningResource .
                                        }
                                        %s
                                    } GROUP BY ?propertyDefiningResource
                                }
                            }
                            UNION
                            {
                                {
                                    SELECT (COUNT(DISTINCT(?property)) AS ?propertyCount)
                                    WHERE {
                                        GRAPH ?taxonomyGraph {
                                            ?property a rdf:Property .
                                            FILTER NOT EXISTS { 
                                                ?property rdfs:isDefinedBy ?x . 
                                            }
                                        }
                                        %s
                                    }
                                }
                                UNION
                                {
                                    SELECT (COUNT(DISTINCT(?statement)) AS ?statementCount)
                                    WHERE {
                                        GRAPH ?contentGraph { 
                                            ?statement a rdf:Statement ; 
                                                rdf:subject ?content ; 
                                                rdf:predicate ?property ; 
                                                team:status ?status . 
                                            ?content team:status ?status . 
                                        }
                                        GRAPH ?taxonomyGraph {
                                            ?property a rdf:Property .
                                            FILTER NOT EXISTS { 
                                                ?property rdfs:isDefinedBy ?x . 
                                            }
                                        }
                                        %s
                                    }
                                }    
                            }
                        }
                    """,
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "",
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "",
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "",
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "");

            if (context == null) {
                construct.setBinding("taxonomyVersionGraph", vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss));
                construct.setBinding("taxonomyGraphStatus", TEAM.CURRENT);
            } else {
                construct.setBinding("taxonomyGraph", vf.createIRI(context));
            }

            BNode blank = vf.createBNode(uuidp.randomUUID().toString());

            construct.setBinding("blank", blank);
            construct.setBinding("contentGraph", vf.createIRI(Namespaces.CONTENT_GRAPH, iss));
            construct.setBinding("status", TEAM.CURRENT);
            
            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }

    @ContextJwt
    @GetMapping(value = "/schema/top-properties", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSchemaTopProperties(@IssParam String iss, @RequestParam(required = false) String context, OutputStream os) {

        log.info("Retrieving top properties. Iss: {}", iss);

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
                        PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                        CONSTRUCT {
                            ?property ?p ?o .
                            ?property team:propertyCount ?propertyCount .
                            ?property team:statementCount ?statementCount .
                        }
                        WHERE {
                            {
                                SELECT ?property (COUNT(DISTINCT(?subProperty)) AS ?propertyCount)
                                WHERE {
                                    GRAPH ?taxonomyGraph {
                                        ?subProperty a rdf:Property .
                                        ?subProperty rdfs:subPropertyOf* ?property .
                                        FILTER NOT EXISTS { 
                                            ?subProperty rdfs:isDefinedBy ?x . 
                                        }
                                    }
                                    %s
                                } GROUP BY ?property
                            }
                            UNION
                            {
                                SELECT ?property (COUNT(DISTINCT(?statement)) AS ?statementCount)
                                WHERE {
                                    GRAPH ?contentGraph { 
                                        ?statement a rdf:Statement ; 
                                            rdf:subject ?content ; 
                                            rdf:predicate ?subProperty ; 
                                            team:status ?status . 
                                        ?content team:status ?status . 
                                    }
                                    GRAPH ?taxonomyGraph {
                                        ?subProperty a rdf:Property .
                                        ?subProperty rdfs:subPropertyOf* ?property .
                                        FILTER NOT EXISTS { 
                                            ?subProperty rdfs:isDefinedBy ?x . 
                                        }
                                    }
                                    %s
                                } GROUP BY ?property
                            }
                            GRAPH ?taxonomyGraph {
                                ?property ?p ?o .
                            }
                            {
                                SELECT ?property 
                                WHERE {
                                    GRAPH ?taxonomyGraph {
                                        ?property a rdf:Property .
                                        FILTER NOT EXISTS { 
                                            ?property rdfs:isDefinedBy ?y . 
                                        }
                                        FILTER NOT EXISTS { 
                                            ?property rdfs:subPropertyOf+ ?superProperty . 
                                            FILTER NOT EXISTS { 
                                                ?superProperty rdfs:isDefinedBy ?z .
                                            }
                                        }
                                    }
                                    %s
                                }
                            }
                        }
    
                    """,
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "",
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "",
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "");

            // Reason to introduce context as opposed to directly binding is an apparent query optimizer issue.
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
    @GetMapping(value = "/schema/top-properties-for-defining-resource", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSchemaTopPropertiesForDefiningResource(@IssParam String iss, @RequestParam String resource, @RequestParam(required = false) String context, OutputStream os) {

        log.info("Retrieving top properties. Iss: {}", iss);

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
                        PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                        CONSTRUCT {
                            ?property ?p ?o .
                            ?property team:propertyCount ?propertyCount .
                            ?property team:statementCount ?statementCount .
                        }
                        WHERE {
                            {
                                SELECT ?property ?p ?o (COUNT(DISTINCT(?subProperty)) AS ?propertyCount)
                                WHERE {
                                    GRAPH ?taxonomyGraph {
                                        ?subProperty a rdf:Property .
                                        ?subProperty rdfs:subPropertyOf* ?property .
                                        ?subProperty rdfs:isDefinedBy ?isDefinedBy .
                                    }
                                    %s
                                } GROUP BY ?property ?p ?o
                            }
                            UNION
                            {
                                SELECT ?property (COUNT(DISTINCT(?statement)) AS ?statementCount)
                                WHERE {
                                    GRAPH ?contentGraph { 
                                        ?statement a rdf:Statement ; 
                                            rdf:subject ?content ; 
                                            rdf:predicate ?subProperty ; 
                                            team:status ?status . 
                                        ?content team:status ?status . 
                                    }
                                    GRAPH ?taxonomyGraph {
                                        ?subProperty a rdf:Property .
                                        ?subProperty rdfs:subPropertyOf* ?property .
                                        ?subProperty rdfs:isDefinedBy ?isDefinedBy .
                                    }
                                    %s
                                } GROUP BY ?property
                            }
                            GRAPH ?taxonomyGraph {
                                ?property ?p ?o .
                            }
                            {
                                SELECT ?property
                                WHERE {
                                    GRAPH ?taxonomyGraph {
                                        ?property a rdf:Property .
                                        ?property rdfs:isDefinedBy ?isDefinedBy .
                                        FILTER NOT EXISTS {
                                            ?property rdfs:subPropertyOf+ ?superProperty .
                                            ?superProperty rdfs:isDefinedBy ?isDefinedBy .
                                        }
                                    }
                                    %s
                                }
                            }
                        }
                    """,
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "",
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "",
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "");

            // Reason to introduce context as opposed to directly binding is an apparent query optimizer issue.
            if (context == null) {
                construct.setBinding("taxonomyVersionGraph", vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss));
                construct.setBinding("taxonomyGraphStatus", TEAM.CURRENT);
            } else {
                construct.setBinding("taxonomyGraph", vf.createIRI(context));
            }
            construct.setBinding("contentGraph", vf.createIRI(Namespaces.CONTENT_GRAPH, iss));
            construct.setBinding("isDefinedBy", vf.createIRI(resource));
            construct.setBinding("status", TEAM.CURRENT);
            
            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }

    @ContextJwt
    @GetMapping(value = "/schema/sub-properties", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSchemaSubProperties(@IssParam String iss, @RequestParam(name = "property") String superProperty, @RequestParam(required = false) String context, OutputStream os) {

        log.info("Retrieving subproperties. Iss: {}", iss);

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
                        PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                        CONSTRUCT {
                            ?property ?p ?o .
                            ?property team:propertyCount ?propertyCount .
                            ?property team:statementCount ?statementCount .
                        }
                        WHERE {
                            {
                                SELECT ?property (COUNT(DISTINCT(?subProperty)) AS ?propertyCount)
                                WHERE {
                                    GRAPH ?taxonomyGraph {
                                        ?subProperty a rdf:Property .
                                        ?subProperty rdfs:subPropertyOf* ?property .
                                        FILTER NOT EXISTS {
                                            ?subProperty rdfs:isDefinedBy ?x .
                                        }
                                    }
                                    %s
                                } GROUP BY ?property
                            }
                            UNION
                            {
                                SELECT ?property (COUNT(DISTINCT(?statement)) AS ?statementCount)
                                WHERE {
                                    GRAPH ?contentGraph { 
                                        ?statement a rdf:Statement ; 
                                            rdf:subject ?content ; 
                                            rdf:predicate ?subProperty ; 
                                            team:status ?status . 
                                        ?content team:status ?status . 
                                    }
                                    GRAPH ?taxonomyGraph {
                                        ?subProperty a rdf:Property .
                                        ?subProperty rdfs:subPropertyOf* ?property .
                                        FILTER NOT EXISTS {
                                            ?subProperty rdfs:isDefinedBy ?x .
                                        }
                                    }
                                    %s
                                } GROUP BY ?property
                            }
                            GRAPH ?taxonomyGraph {
                                ?property ?p ?o .
                            }
                            {
                                SELECT ?property
                                WHERE {
                                    GRAPH ?taxonomyGraph {
                                        {
                                            ?property a rdf:Property .
                                            ?property rdfs:subPropertyOf ?superProperty .
                                            FILTER NOT EXISTS {
                                                ?property rdfs:isDefinedBy ?y .
                                            }
                                        }
                                        UNION {
                                            ?sub a rdf:Property .
                                            ?sub rdfs:subPropertyOf ?superProperty .
                                            ?sub rdfs:isDefinedBy ?subDefiningResource .
                                
                                            ?property a rdf:Property .
                                            ?property rdfs:subPropertyOf* ?sub .
                                            FILTER NOT EXISTS {
                                                ?property rdfs:isDefinedBy ?y .
                                            }
                                
                                            FILTER NOT EXISTS {
                                                ?subSubSub a rdf:Property .
                                                ?subSubSub rdfs:subPropertyOf+ ?property .
                                            }
                                        }
                                    }
                                    %s
                                }
                            }
                        }
                    """,
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "",
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "",
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "");

            if (context == null) {
                construct.setBinding("taxonomyVersionGraph", vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss));
                construct.setBinding("taxonomyGraphStatus", TEAM.CURRENT);
            } else {
                construct.setBinding("taxonomyGraph", vf.createIRI(context));
            }
            construct.setBinding("contentGraph", vf.createIRI(Namespaces.CONTENT_GRAPH, iss));
            construct.setBinding("status", TEAM.CURRENT);
            construct.setBinding("superProperty", vf.createIRI(superProperty));

            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }

    @ContextJwt
    @GetMapping(value = "/schema/sub-properties-for-defining-resource", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSchemaSubPropertiesForDefiningResource(@IssParam String iss, @RequestParam String resource, @RequestParam(name = "property") String superProperty, @RequestParam(required = false) String context, OutputStream os) {

        log.info("Retrieving subproperties. Iss: {}", iss);

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
                        PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                        CONSTRUCT {
                            ?property ?p ?o .
                            ?property team:propertyCount ?propertyCount .
                            ?property team:statementCount ?statementCount .
                        }
                        WHERE {
                            {
                                SELECT ?property (COUNT(DISTINCT(?subProperty)) AS ?propertyCount)
                                WHERE {
                                    GRAPH ?taxonomyGraph {
                                        ?subProperty a rdf:Property .
                                        ?subProperty rdfs:subPropertyOf* ?property .
                                        ?subProperty rdfs:isDefinedBy ?propertyDefiningResource .
                                        ?property rdfs:isDefinedBy ?propertyDefiningResource .
                                    }
                                    %s
                                } GROUP BY ?property
                            }
                            UNION
                            {
                                SELECT ?property (COUNT(DISTINCT(?statement)) AS ?statementCount)
                                WHERE {
                                    GRAPH ?contentGraph { 
                                        ?statement a rdf:Statement ; 
                                            rdf:subject ?content ; 
                                            rdf:predicate ?subProperty ; 
                                            team:status ?status . 
                                        ?content team:status ?status . 
                                    }
                                    GRAPH ?taxonomyGraph {
                                        ?subProperty a rdf:Property .
                                        ?subProperty rdfs:subPropertyOf* ?property .
                                        ?subProperty rdfs:isDefinedBy ?propertyDefiningResource .
                                        ?property rdfs:isDefinedBy ?propertyDefiningResource .
                                    }
                                    %s
                                } GROUP BY ?property
                            }
                            GRAPH ?taxonomyGraph {
                                ?property ?p ?o .
                            }
                            {
                                SELECT ?property 
                                WHERE {
                                    GRAPH ?taxonomyGraph {
                                        {
                                            ?property a rdf:Property .
                                            ?property rdfs:subPropertyOf ?superProperty .
                                            ?property rdfs:isDefinedBy ?isDefinedBy .
                                        }
                                        UNION
                                        {
                                            ?sub a rdf:Property .
                                            ?sub rdfs:subPropertyOf ?superProperty .
                                            ?sub rdfs:isDefinedBy ?subDefiningResource .
                                            FILTER (?subDefiningResource != ?isDefinedBy) .
                                
                                            ?property a rdf:Property .
                                            ?property rdfs:subPropertyOf* ?sub .
                                            ?property rdfs:isDefinedBy ?subSubDefiningResource .
                                            FILTER (?subSubDefiningResource = ?isDefinedBy) .
                                
                                            FILTER NOT EXISTS {
                                                ?subSubSub a rdf:Property .
                                                ?subSubSub rdfs:subPropertyOf+ ?property .
                                            }
                                        }
                                    }
                                    %s
                                }
                            }
                        }
                    """,
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "",
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "",
                    context == null ? "{ SELECT ?taxonomyGraph WHERE { GRAPH ?taxonomyVersionGraph { ?taxonomyGraph a team:TaxonomyGraph ; team:status ?taxonomyGraphStatus . } } }" : "");

            if (context == null) {
                construct.setBinding("taxonomyVersionGraph", vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss));
                construct.setBinding("taxonomyGraphStatus", TEAM.CURRENT);
            } else {
                construct.setBinding("taxonomyGraph", vf.createIRI(context));
            }
            construct.setBinding("contentGraph", vf.createIRI(Namespaces.CONTENT_GRAPH, iss));
            construct.setBinding("status", TEAM.CURRENT);
            construct.setBinding("isDefinedBy", vf.createIRI(resource));
            construct.setBinding("superProperty", vf.createIRI(superProperty));

            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }

    @ContextJwt
    @GetMapping(value = "/schema/content-with-property-as-predicate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSchemaContentWithPropertyAsPredicate(@IssParam String iss, @RequestParam(name = "property") String propertyAsPredicate, OutputStream os) {

        log.info("Retrieving content with property as predicate. Iss: {}. Property: {}", iss, propertyAsPredicate);

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
                            ?property ?object .
                    }
                    WHERE {
                        GRAPH ?contentGraph {
                            ?statementS a rdf:Statement ;
                                rdf:subject ?contentS ;
                                rdf:predicate ?property ;
                                rdf:object ?object ;
                                team:status ?status .
                            ?contentS a ?contentClass ;
                                team:status ?status ;
                                ?contentP ?contentO .
                        }
                    } 
                    """);
            construct.setBinding("contentGraph", vf.createIRI(Namespaces.CONTENT_GRAPH, iss));
            construct.setBinding("property",  vf.createIRI(propertyAsPredicate));
            construct.setBinding("status", TEAM.CURRENT);
            
            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }
}