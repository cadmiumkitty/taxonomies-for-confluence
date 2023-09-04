package com.dalstonsemantics.confluence.semantics.cloud;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.BNode;
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
public class SchemaClassController {

    private static final String ACTIVE = "active";
  
    private Rdf4jRepositoryPool taxonomyRepositoryPool;

    private AtlassianHostRepository atlassianHostRepository;
    private AddOnService addOnService;
    private UUIDProvider uuidp;
    private boolean enforceLicense;

    public SchemaClassController(@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool, 
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
    @GetMapping(value = "/schema/defining-resources-for-classes", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSchemaDefiningResourcesForClasses(@IssParam String iss, @RequestParam(required = false) String context, OutputStream os) {

        log.info("Retrieving defining resources for classes. Iss: {}", iss);

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
                        ?classDefiningResource ?p ?o .
                        ?classDefiningResource team:classCount ?definingResourceClassCount .
                        ?classDefiningResource team:statementCount ?definingResourceStatementCount .
                        ?classDefiningResource a team:DefiningResource .
                        ?blank rdfs:label "Classes with no defining resources" .
                        ?blank team:classCount ?classCount .
                        ?blank team:statementCount ?statementCount .
                        ?blank a team:BlankDefiningResource .
                    }
                    WHERE {
                        {
                            {
                                SELECT ?classDefiningResource ?p ?o (COUNT(DISTINCT(?class)) AS ?definingResourceClassCount)
                                WHERE {
                                    GRAPH ?taxonomyGraph {
                                        ?classDefiningResource ?p ?o .
                                        ?class a rdfs:Class .
                                        ?class rdfs:isDefinedBy ?classDefiningResource .
                                    }
                                    %s
                                } GROUP BY ?classDefiningResource ?p ?o
                            }
                            UNION
                            {
                                SELECT ?classDefiningResource (COUNT(DISTINCT(?statement)) AS ?definingResourceStatementCount)
                                WHERE {
                                    GRAPH ?contentGraph { 
                                        ?statement a rdf:Statement ; 
                                            rdf:subject ?content ; 
                                            rdf:object ?class ; 
                                            team:status ?status . 
                                        ?content team:status ?status . 
                                    }
                                    GRAPH ?taxonomyGraph {
                                        ?class a rdfs:Class .
                                        ?class rdfs:isDefinedBy ?classDefiningResource .
                                    }
                                    %s
                                } GROUP BY ?classDefiningResource
                            }
                        }
                        UNION
                        {
                            {
                                SELECT (COUNT(DISTINCT(?class)) AS ?classCount)
                                WHERE {
                                    GRAPH ?taxonomyGraph {
                                        ?class a rdfs:Class .
                                        FILTER NOT EXISTS { 
                                            ?class rdfs:isDefinedBy ?x . 
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
                                            rdf:object ?class ; 
                                            team:status ?status . 
                                        ?content team:status ?status . 
                                    }
                                    GRAPH ?taxonomyGraph {
                                        ?class a rdfs:Class .
                                        FILTER NOT EXISTS { 
                                            ?class rdfs:isDefinedBy ?x . 
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
    @GetMapping(value = "/schema/top-classes", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSchemaTopClasses(@IssParam String iss, @RequestParam(required = false) String context, OutputStream os) {

        log.info("Retrieving top classes. Iss: {}", iss);

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
                        ?class ?p ?o .
                        ?class team:classCount ?classCount .
                        ?class team:statementCount ?statementCount .
                    }
                    WHERE {
                        {
                            SELECT ?class (COUNT(DISTINCT(?subClass)) AS ?classCount)
                            WHERE {
                                GRAPH ?taxonomyGraph {
                                    ?subClass a rdfs:Class .
                                    ?subClass rdfs:subClassOf* ?class .
                                    FILTER NOT EXISTS { 
                                        ?subClass rdfs:isDefinedBy ?x . 
                                    }
                                }
                                %s
                            } GROUP BY ?class
                        }
                        UNION
                        {
                            SELECT ?class (COUNT(DISTINCT(?statement)) AS ?statementCount)
                            WHERE {
                                GRAPH ?contentGraph { 
                                    ?statement a rdf:Statement ; 
                                        rdf:subject ?content ; 
                                        rdf:object ?subClass ; 
                                        team:status ?status . 
                                    ?content team:status ?status . 
                                }
                                GRAPH ?taxonomyGraph {
                                    ?subClass a rdfs:Class .
                                    ?subClass rdfs:subClassOf* ?class .
                                    FILTER NOT EXISTS { 
                                        ?subClass rdfs:isDefinedBy ?x . 
                                    }
                                }
                                %s
                            } GROUP BY ?class
                        }
                        GRAPH ?taxonomyGraph {
                            ?class ?p ?o .
                        }
                        {
                            SELECT ?class 
                            WHERE {
                                GRAPH ?taxonomyGraph {
                                    ?class a rdfs:Class .
                                    FILTER NOT EXISTS { 
                                        ?class rdfs:isDefinedBy ?y . 
                                    }
                                    FILTER NOT EXISTS { 
                                        ?class rdfs:subClassOf+ ?superClass . 
                                        FILTER NOT EXISTS { 
                                            ?superClass rdfs:isDefinedBy ?z .
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
    @GetMapping(value = "/schema/top-classes-for-defining-resource", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSchemaTopClassesForDefiningResource(@IssParam String iss, @RequestParam String resource, @RequestParam(required = false) String context, OutputStream os) {

        log.info("Retrieving top classes. Iss: {}", iss);

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
                        ?class ?p ?o .
                        ?class team:classCount ?classCount .
                        ?class team:statementCount ?statementCount .
                    }
                    WHERE {
                        {
                            SELECT ?class (COUNT(DISTINCT(?subClass)) AS ?classCount)
                            WHERE {
                                GRAPH ?taxonomyGraph {
                                    ?subClass a rdfs:Class .
                                    ?subClass rdfs:subClassOf* ?class .
                                    ?subClass rdfs:isDefinedBy ?isDefinedBy .
                                }
                                %s
                            } GROUP BY ?class
                        }
                        UNION
                        {
                            SELECT ?class (COUNT(DISTINCT(?statement)) AS ?statementCount)
                            WHERE {
                                GRAPH ?contentGraph { 
                                    ?statement a rdf:Statement ; 
                                        rdf:subject ?content ; 
                                        rdf:object ?subClass ; 
                                        team:status ?status . 
                                    ?content team:status ?status . 
                                }
                                GRAPH ?taxonomyGraph {
                                    ?subClass a rdfs:Class .
                                    ?subClass rdfs:subClassOf* ?class .
                                    ?subClass rdfs:isDefinedBy ?isDefinedBy .
                                }
                                %s
                            } GROUP BY ?class
                        }
                        GRAPH ?taxonomyGraph {
                            ?class ?p ?o .
                        }
                        {
                            SELECT ?class 
                            WHERE {
                                GRAPH ?taxonomyGraph {
                                    ?class a rdfs:Class .
                                    ?class rdfs:isDefinedBy ?isDefinedBy .
                                    FILTER NOT EXISTS {
                                        ?class rdfs:subClassOf+ ?superClass .
                                        ?superClass rdfs:isDefinedBy ?isDefinedBy .
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
    @GetMapping(value = "/schema/sub-classes", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSchemaSubClasses(@IssParam String iss, @RequestParam(name = "class") String superClass, @RequestParam(required = false) String context, OutputStream os) {

        log.info("Retrieving subclasses. Iss: {}", iss);

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
                        ?class ?p ?o .
                        ?class team:classCount ?classCount .
                        ?class team:statementCount ?statementCount .
                    }
                    WHERE {
                        {
                            SELECT ?class (COUNT(DISTINCT(?subClass)) AS ?classCount)
                            WHERE {
                                GRAPH ?taxonomyGraph {
                                    ?subClass a rdfs:Class .
                                    ?subClass rdfs:subClassOf* ?class .
                                    FILTER NOT EXISTS {
                                        ?subClass rdfs:isDefinedBy ?x .
                                    }
                                }
                                %s
                            } GROUP BY ?class
                        }
                        UNION
                        {
                            SELECT ?class (COUNT(DISTINCT(?statement)) AS ?statementCount)
                            WHERE {
                                GRAPH ?contentGraph { 
                                    ?statement a rdf:Statement ; 
                                        rdf:subject ?content ; 
                                        rdf:object ?subClass ; 
                                        team:status ?status . 
                                    ?content team:status ?status . 
                                }
                                GRAPH ?taxonomyGraph {
                                    ?subClass a rdfs:Class .
                                    ?subClass rdfs:subClassOf* ?class .
                                    FILTER NOT EXISTS {
                                        ?subClass rdfs:isDefinedBy ?x .
                                    }
                                }
                                %s
                            } GROUP BY ?class
                        }
                        GRAPH ?taxonomyGraph {
                            ?class ?p ?o .
                        }
                        {
                            SELECT ?class 
                            WHERE {
                                GRAPH ?taxonomyGraph {
                                    {
                                        ?class a rdfs:Class .
                                        ?class rdfs:subClassOf ?superClass .
                                        FILTER NOT EXISTS {
                                            ?class rdfs:isDefinedBy ?y .
                                        }
                                    }
                                    UNION {
                                        ?sub a rdfs:Class .
                                        ?sub rdfs:subClassOf ?superClass .
                                        ?sub rdfs:isDefinedBy ?subDefiningResource .
                            
                                        ?class a rdfs:Class .
                                        ?class rdfs:subClassOf* ?sub .
                                        FILTER NOT EXISTS {
                                            ?class rdfs:isDefinedBy ?y .
                                        }
                            
                                        FILTER NOT EXISTS {
                                            ?subSubSub a rdfs:Class .
                                            ?subSubSub rdfs:subClassOf+ ?class .
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
            construct.setBinding("superClass", vf.createIRI(superClass));

            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }

    @ContextJwt
    @GetMapping(value = "/schema/sub-classes-for-defining-resource", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSchemaSubClassesForDefiningResource(@IssParam String iss, @RequestParam String resource, @RequestParam(name = "class") String superClass, @RequestParam(required = false) String context, OutputStream os) {

        log.info("Retrieving subclasses. Iss: {}", iss);

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
                        ?class ?p ?o .
                        ?class team:classCount ?classCount .
                        ?class team:statementCount ?statementCount .
                    }
                    WHERE {
                        {
                            SELECT ?class (COUNT(DISTINCT(?subClass)) AS ?classCount)
                            WHERE {
                                GRAPH ?taxonomyGraph {
                                    ?subClass a rdfs:Class .
                                    ?subClass rdfs:subClassOf* ?class .
                                    ?subClass rdfs:isDefinedBy ?classDefiningResource .
                                    ?class rdfs:isDefinedBy ?classDefiningResource .
                                }
                                %s
                            } GROUP BY ?class
                        }
                        UNION
                        {
                            SELECT ?class (COUNT(DISTINCT(?statement)) AS ?statementCount)
                            WHERE {
                                GRAPH ?contentGraph { 
                                    ?statement a rdf:Statement ; 
                                        rdf:subject ?content ; 
                                        rdf:object ?subClass ; 
                                        team:status ?status . 
                                    ?content team:status ?status . 
                                }
                                GRAPH ?taxonomyGraph {
                                    ?subClass a rdfs:Class .
                                    ?subClass rdfs:subClassOf* ?class .
                                    ?subClass rdfs:isDefinedBy ?classDefiningResource .
                                    ?class rdfs:isDefinedBy ?classDefiningResource .
                                }
                                %s
                            } GROUP BY ?class
                        }
                        GRAPH ?taxonomyGraph {
                            ?class ?p ?o .
                        }
                        {
                            SELECT ?class 
                            WHERE {
                                GRAPH ?taxonomyGraph {
                                    {
                                        ?class a rdfs:Class .
                                        ?class rdfs:subClassOf ?superClass .
                                        ?class rdfs:isDefinedBy ?isDefinedBy .
                                    }
                                    UNION
                                    {
                                        ?sub a rdfs:Class .
                                        ?sub rdfs:subClassOf ?superClass .
                                        ?sub rdfs:isDefinedBy ?subDefiningResource .
                                        FILTER (?subDefiningResource != ?isDefinedBy) .
                            
                                        ?class a rdfs:Class .
                                        ?class rdfs:subClassOf* ?sub .
                                        ?class rdfs:isDefinedBy ?subSubDefiningResource .
                                        FILTER (?subSubDefiningResource = ?isDefinedBy) .
                            
                                        FILTER NOT EXISTS {
                                            ?subSubSub a rdfs:Class .
                                            ?subSubSub rdfs:subClassOf+ ?class .
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
            construct.setBinding("superClass", vf.createIRI(superClass));

            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }

    @ContextJwt
    @GetMapping(value = "/schema/content-with-class-as-object", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getSchemaContentWithClassAsObject(@IssParam String iss, @RequestParam(name = "class") String classAsObject, OutputStream os) {

        log.info("Retrieving content with class as object. Iss: {}. Class: {}", iss, classAsObject);

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
                            ?property ?class .
                    }
                    WHERE {
                        GRAPH ?contentGraph {
                            ?statementS a rdf:Statement ;
                                rdf:subject ?contentS ;
                                rdf:predicate ?property ;
                                rdf:object ?class ;
                                team:status ?status .
                            ?contentS a ?contentClass ;
                                team:status ?status ;
                                ?contentP ?contentO .
                        }
                    } 
                    """);
            construct.setBinding("contentGraph", vf.createIRI(Namespaces.CONTENT_GRAPH, iss));
            construct.setBinding("class",  vf.createIRI(classAsObject));
            construct.setBinding("status", TEAM.CURRENT);
            
            connection.setIsolationLevel(IsolationLevels.NONE);

            construct.evaluate(writer);
        }
    }
    
    @GetMapping(value = "/class-byline")
    public String getTypeByline(@RequestParam String lic) {

        if (enforceLicense && !ACTIVE.equals(lic)) {
            throw new NoActiveLicenseException();
        }

        return "class-byline";
    }

    @ContextJwt
    @GetMapping(value = "/class/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void queryClasses(@IssParam String iss, @RequestParam(name = "q") String query, OutputStream os) throws IOException {

        log.info("Retrieving classes. Iss: {}. Search term: {}", iss, query);

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
                    CONSTRUCT {
                        ?s ?p ?o .
                        ?s team:searchScore ?searchScore .
                        ?defined_by a team:DefiningResource .
                        ?defined_by ?defined_by_predicate ?defined_by_object .
                    }
                    WHERE {
                        GRAPH ?taxonomyGraph {
                            ?s ?p ?o .
                            OPTIONAL {
                                ?s rdfs:isDefinedBy ?defined_by .
                                ?defined_by ?defined_by_predicate ?defined_by_object .
                            }
                            { 
                                SELECT DISTINCT ?s ?searchScore {
                                    ?s a rdfs:Class ;
                                        rdfs:label ?label .
                                    BIND (xsd:float(STRLEN(?q) / STRLEN(?label)) AS ?searchScore)
                                    FILTER (CONTAINS(LCASE(?label),LCASE(?q)))
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
}