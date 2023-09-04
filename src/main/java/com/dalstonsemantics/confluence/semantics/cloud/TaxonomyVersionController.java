package com.dalstonsemantics.confluence.semantics.cloud;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.ContextJwt;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.dalstonsemantics.confluence.semantics.cloud.domain.addon.AddOn;
import com.dalstonsemantics.confluence.semantics.cloud.domain.addon.License;
import com.dalstonsemantics.confluence.semantics.cloud.domain.user.User;
import com.dalstonsemantics.confluence.semantics.cloud.processor.EventDispatcher;
import com.dalstonsemantics.confluence.semantics.cloud.provider.BlobClientProvider;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.resolver.IssParam;
import com.dalstonsemantics.confluence.semantics.cloud.resolver.SubParam;
import com.dalstonsemantics.confluence.semantics.cloud.service.AddOnService;
import com.dalstonsemantics.confluence.semantics.cloud.service.ContentService;
import com.dalstonsemantics.confluence.semantics.cloud.service.UserService;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class TaxonomyVersionController {

    private Rdf4jRepositoryPool taxonomyRepositoryPool;

    private AtlassianHostRepository atlassianHostRepository;
    private UserService userService;
    private AddOnService addOnService;
    private ProvenanceModelEnricher provenanceModelEnricher;
    private UUIDProvider uuidp;
    private BlobClientProvider blobClientProvider;
    private EventDispatcher dispatcher;
    private boolean enforceLicense;
    private String importFileBlobContainerName;


    public TaxonomyVersionController(@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool, 
            @Autowired AtlassianHostRepository atlassianHostRepository, 
            @Autowired ContentService contentService, 
            @Autowired UserService userService, 
            @Autowired AddOnService addOnService, 
            @Autowired ProvenanceModelEnricher provenanceModelEnricher,
            @Autowired UUIDProvider uuidp,
            @Autowired BlobClientProvider blobClientProvider,
            @Autowired EventDispatcher dispatcher,
            @Value("${addon.license.enforce}") boolean enforceLicense,
            @Value("${addon.blobs.import-file}") String importFileBlobContainerName) {
        this.taxonomyRepositoryPool = taxonomyRepositoryPool;
        this.atlassianHostRepository = atlassianHostRepository;
        this.userService = userService;
        this.addOnService = addOnService;
        this.provenanceModelEnricher = provenanceModelEnricher;
        this.uuidp = uuidp;
        this.blobClientProvider = blobClientProvider;
        this.dispatcher = dispatcher;
        this.enforceLicense = enforceLicense;
        this.importFileBlobContainerName = importFileBlobContainerName;
    }

    @ContextJwt
    @GetMapping(value = "/taxonomy/version", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public void getVersion(@IssParam String iss, @SubParam String sub, @RequestParam(required = false) String context, OutputStream os) throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Retrieving taxonomy versions. Iss: {}", iss);

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
            IRI taxonomyVersionGraph = vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss);

            RDFWriter writer = Rio.createWriter(RDFFormat.JSONLD, os);
            writer.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.FLATTEN);

            GraphQuery query = SPARQLFactory.graphQueryWithConnection(
                    connection, 
                    """
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    PREFIX prov: <http://www.w3.org/ns/prov#>
                    CONSTRUCT {
                        ?taxonomyGraph a team:TaxonomyGraph ;
                            team:status ?status ;
                            team:statusTransitionErrorMessage ?statusTransitionErrorMessage ;
                            team:conceptCount ?conceptCount ;
                            team:classCount ?classCount ;
                            team:propertyCount ?propertyCount ;
                            team:insertedConceptCount ?insertedConceptCount ;
                            team:updatedConceptCount ?updatedConceptCount ;
                            team:deletedConceptCount ?deletedConceptCount ;
                            team:insertedClassCount ?insertedClassCount ;
                            team:updatedClassCount ?updatedClassCount ;
                            team:deletedClassCount ?deletedClassCount ;
                            team:insertedPropertyCount ?insertedPropertyCount ;
                            team:updatedPropertyCount ?updatedPropertyCount ;
                            team:deletedPropertyCount ?deletedPropertyCount ;
                            team:impactedContentCount ?impactedContentCount ;
                            team:processedContentCount ?processedContentCount ;
                            team:failedContentCount ?failedContentCount ;
                            team:taxonomyGraphSequenceNumber ?taxonomyGraphSequenceNumber .
                        ?activity ?activityP ?activityO .
                        ?agent ?agentP ?agentO .
                    }
                    WHERE {
                        GRAPH ?taxonomyVersionGraph {
                            OPTIONAL { ?taxonomyGraph team:statusTransitionErrorMessage ?statusTransitionErrorMessage }
                            OPTIONAL { ?taxonomyGraph team:conceptCount ?conceptCount }
                            OPTIONAL { ?taxonomyGraph team:classCount ?classCount }
                            OPTIONAL { ?taxonomyGraph team:propertyCount ?propertyCount }
                            OPTIONAL { ?taxonomyGraph team:insertedConceptCount ?insertedConceptCount }
                            OPTIONAL { ?taxonomyGraph team:updatedConceptCount ?updatedConceptCount }
                            OPTIONAL { ?taxonomyGraph team:deletedConceptCount ?deletedConceptCount }
                            OPTIONAL { ?taxonomyGraph team:insertedClassCount ?insertedClassCount }
                            OPTIONAL { ?taxonomyGraph team:updatedClassCount ?updatedClassCount }
                            OPTIONAL { ?taxonomyGraph team:deletedClassCount ?deletedClassCount }
                            OPTIONAL { ?taxonomyGraph team:insertedPropertyCount ?insertedPropertyCount }
                            OPTIONAL { ?taxonomyGraph team:updatedPropertyCount ?updatedPropertyCount }
                            OPTIONAL { ?taxonomyGraph team:deletedPropertyCount ?deletedPropertyCount }
                            OPTIONAL { ?taxonomyGraph team:impactedContentCount ?impactedContentCount }
                            OPTIONAL { ?taxonomyGraph team:processedContentCount ?processedContentCount }
                            OPTIONAL { ?taxonomyGraph team:failedContentCount ?failedContentCount }
                            ?activity a prov:Activity ;
                                prov:wasAssociatedWith ?agent ;
                                prov:generated ?taxonomyGraph ;
                                ?activityP ?activityO .
                            ?agent a prov:Agent ;
                                ?agentP ?agentO .
                            ?taxonomyGraph team:status ?status .
                            { 
                                SELECT ?taxonomyGraph (COUNT(?previousTaxonomyGraph) AS ?taxonomyGraphSequenceNumber) 
                                WHERE {
                                    ?taxonomyGraph a team:TaxonomyGraph ;
                                        team:previousTaxonomyGraph* ?previousTaxonomyGraph .
                                } GROUP BY ?taxonomyGraph ORDER BY DESC(?taxonomyGraphSequenceNumber) LIMIT 10
                            }     
                        } 
                    }
                    """);
            query.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
            if (context != null) {
                query.setBinding("taxonomyGraph", vf.createIRI(context));
            }

            connection.setIsolationLevel(IsolationLevels.NONE);

            try (GraphQueryResult result = query.evaluate()) {

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

    @ContextJwt
    @PostMapping(value = "/taxonomy/version/draft/import-file")
    @ResponseStatus(value = HttpStatus.OK)
    public void postImportFile(@IssParam String iss, @SubParam String sub, @RequestParam MultipartFile file) throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Uploading taxonomy. Iss: {}. Sub: {}.", iss, sub);

        validateLicenseAndOperations(iss, sub);

        RDFFormat format = Rio.getParserFormatForFileName(file.getOriginalFilename()).orElse(RDFFormat.TURTLE);
        String blobName = uuidp.randomUUID().toString();

        BlobClient blobClient = blobClientProvider.getBlobClient(importFileBlobContainerName, blobName);
        blobClient.upload(file.getInputStream(), file.getSize());
        blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(format.getDefaultMIMEType()));

        updateStatusAndFireTransitionEvent(iss, sub, TEAM.DRAFT, TEAM.IMPORTING, TEAM.IMPORT_FILE_EVENT, 
                new HashMap<>() {{ 
                    put(TEAM.BLOB_NAME, Arrays.asList(blobName));
                }});
    }

    @ContextJwt
    @PostMapping(value = "/taxonomy/version/draft/import-catalog", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @ResponseStatus(value = HttpStatus.OK)
    public void postImportResource(@IssParam String iss, @SubParam String sub, @RequestParam List<String> scheme) throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Uploading from catalog. Iss: {}. Sub: {}.", iss, sub);

        validateLicenseAndOperations(iss, sub);

        List<String> resources = new LinkedList<>();
        for (String s : scheme) {
            switch (s) {
                case "https://dalstonsemantics.com/ns/au/gov/abs/anzsic/scheme":
                    resources.add("https://github.com/cadmiumkitty/anzsic-taxonomy/releases/download/v1.0.0/anzsic.ttl");
                    break;
                case "https://dalstonsemantics.com/ns/org/isbn-international/978-0124158290/scheme":
                    resources.add("https://github.com/cadmiumkitty/policy-taxonomy/releases/download/v1.0.0/policy-taxonomy.ttl");
                    break;
                case "https://dalstonsemantics.com/taxonomy/sdlc/scheme":
                    resources.add("https://github.com/cadmiumkitty/sdlc-document-types-taxonomy/releases/download/v1.0.0/sdlc-document-types-taxonomy.ttl");
                    break;
                case "https://dalstonsemantics.com/ns/org/opengroup/pubs/architecture/togaf9-doc/arch/chap37.html#scheme":
                    resources.add("https://github.com/cadmiumkitty/togaf-architecture-repository-document-types-taxonomy/releases/download/v1.0.0/togaf-architecture-repository-document-types-taxonomy.ttl");
                    break;
                case "http://www.semanticweb.org/ontologies/2020/4/VocabularyTOGAFContentMetamodel.skos#scheme":
                    resources.add("https://github.com/cadmiumkitty/togaf-content-metamodel-ontology/releases/download/v2.0.1/VocabularyTOGAFContentMetamodelV2.ttl");
                    break;
                case "http://www.w3.org/1999/02/22-rdf-syntax-ns#":
                    resources.add("https://github.com/cadmiumkitty/data-governance/releases/download/v2.0.0/22-rdf-syntax-ns.ttl");
                    break;
                case "http://www.w3.org/2000/01/rdf-schema#":
                    resources.add("https://github.com/cadmiumkitty/data-governance/releases/download/v2.0.0/rdf-schema.ttl");
                    break;
                case "https://schema.org/":
                    resources.add("https://schema.org/version/latest/schemaorg-current-https.ttl");
                    break;
                case "https://dalstonsemantics.com/dg/Schema":
                    resources.add("https://github.com/cadmiumkitty/data-governance/releases/download/v2.0.0/data-governance.ttl");
                    break;
                default:
                    throw new NoSuchSchemeException();
            }
        }

        updateStatusAndFireTransitionEvent(iss, sub, TEAM.DRAFT, TEAM.IMPORTING, TEAM.IMPORT_RESOURCE_EVENT, 
                new HashMap<>() {{ 
                    put(TEAM.RESOURCE, resources);
                }});
    }

    @ContextJwt
    @PostMapping(value = "/taxonomy/version/draft/copy-from-current")
    @ResponseStatus(value = HttpStatus.OK)
    public void postCopyFromCurrentSnapshot(@IssParam String iss, @SubParam String sub) throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Firing CopyFromCurrentSnapshotEvent. Iss: {}. Sub: {}", iss, sub);

        validateLicenseAndOperations(iss, sub);
        updateStatusAndFireTransitionEvent(iss, sub, TEAM.DRAFT, TEAM.COPYING, TEAM.COPY_FROM_CURRENT_EVENT, new HashMap<>());
    }

    @ContextJwt
    @PostMapping(value = "/taxonomy/version/draft/calculate-content-impact")
    @ResponseStatus(value = HttpStatus.OK)
    public void postCalculateContentImpact(@IssParam String iss, @SubParam String sub) throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Firing CalculateContentImpactEvent. Iss: {}. Sub: {}", iss, sub);

        validateLicenseAndOperations(iss, sub);
        updateStatusAndFireTransitionEvent(iss, sub, TEAM.DRAFT, TEAM.CALCULATING_CONTENT_IMPACT, TEAM.CALCULATE_CONTENT_IMPACT_EVENT, new HashMap<>());
    }

    @ContextJwt
    @PostMapping(value = "/taxonomy/version/draft/transition-to-current")
    @ResponseStatus(value = HttpStatus.OK)
    public void postTransitionToCurrent(@IssParam String iss, @SubParam String sub) throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Firing TransitionToCurrentEvent. Iss: {}. Sub: {}", iss, sub);

        validateLicenseAndOperations(iss, sub);
        updateStatusAndFireTransitionEvent(iss, sub, TEAM.AWAITING_TRANSITION_TO_CURRENT, TEAM.TRANSITIONING_TO_CURRENT, TEAM.TRANSITION_TO_CURRENT_EVENT, new HashMap<>());
    }

    @ContextJwt
    @PostMapping(value = "/taxonomy/version/draft/cancel-transition-to-current")
    @ResponseStatus(value = HttpStatus.OK)
    public void postCancelTransitionToCurrent(@IssParam String iss, @SubParam String sub) throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Firing TransitionToCurrentEvent. Iss: {}. Sub: {}", iss, sub);

        validateLicenseAndOperations(iss, sub);
        updateStatusAndFireTransitionEvent(iss, sub, TEAM.AWAITING_TRANSITION_TO_CURRENT, TEAM.CANCELLING_TRANSITION_TO_CURRENT, TEAM.CANCEL_TRANSITION_TO_CURRENT_EVENT, new HashMap<>());
    }

    @ContextJwt
    @PostMapping(value = "/taxonomy/version/draft/clear")
    @ResponseStatus(value = HttpStatus.OK)
    public void postClear(@IssParam String iss, @SubParam String sub) throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        log.info("Firing ClearEvent. Iss: {}. Sub: {}", iss, sub);

        validateLicenseAndOperations(iss, sub);
        updateStatusAndFireTransitionEvent(iss, sub, TEAM.DRAFT, TEAM.CLEARING, TEAM.CLEAR_EVENT, new HashMap<>());
    }

    private void validateLicenseAndOperations(String iss, String sub) {

        AtlassianHost host = atlassianHostRepository.findById(iss).get();

        AddOn addOn = addOnService.getAddOn(host);
        License lic = addOn.getLicense();

        log.debug("Checking license. AddOn: {}. License: {}. Host: {}", addOn, lic, host);

        if (enforceLicense && lic != null && !lic.getActive()) {
            throw new NoActiveLicenseException();
        }

        User user = userService.getUserByAccountId(host, sub);

        log.debug("Checking permissions. User: {}. Host: {}", user, host);

        if (user.getOperations().stream().filter(o -> "administer".equals(o.getOperation()) && "application".equals(o.getTargetType())).count() == 0) {
            throw new NotAuthorizedToUploadTaxonomyException();
        }
    }

    private void updateStatusAndFireTransitionEvent(String iss, String sub,
            Literal preStatus, Literal postStatus, IRI eventClass, Map<IRI, List<String>> customTuples) {

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(iss);

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            ValueFactory vf = taxonomyRepository.getValueFactory();
            IRI taxonomyVersionGraph = vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, iss);

            TupleQuery selectTargetTaxonomy = SPARQLFactory.tupleQueryWithConnection(
                    connection,
                    """
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    SELECT ?targetTaxonomyGraph
                    WHERE {
                        GRAPH ?taxonomyVersionGraph {
                            ?targetTaxonomyGraph a team:TaxonomyGraph ;
                                team:status ?status .
                        }
                    }
                    """);
            selectTargetTaxonomy.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
            selectTargetTaxonomy.setBinding("status", preStatus);

            connection.setIsolationLevel(IsolationLevels.READ_UNCOMMITTED);

            connection.begin();
        
            try (TupleQueryResult selectTargetTaxonomyResult = selectTargetTaxonomy.evaluate()) {

                if (selectTargetTaxonomyResult.hasNext()) {

                    IRI event = vf.createIRI(Namespaces.EVENT, uuidp.randomUUID().toString());
                    IRI targetTaxonomyGraph = (IRI)selectTargetTaxonomyResult.next().getBinding("targetTaxonomyGraph").getValue();
                                    
                    ModelBuilder mb = new ModelBuilder();
                    mb.setNamespace(TEAM.PREFIX, TEAM.NAMESPACE);
                    mb.defaultGraph()
                        .subject(event)
                            .add(RDF.TYPE, eventClass)
                            .add(TEAM.CLIENT_KEY, iss)
                            .add(TEAM.ACCOUNT_ID, sub)
                            .add(TEAM.TARGET_TAXONOMY_GRAPH, targetTaxonomyGraph);
                    
                    customTuples.forEach((subject, predicateList) -> 
                            predicateList.forEach(predicate -> 
                                    mb.defaultGraph().subject(event).add(subject, vf.createLiteral(predicate))));

                    Model eventModel = mb.build();

                    Update delete = SPARQLFactory.updateWithConnection(
                            connection,
                            """
                            PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                            DELETE {
                                GRAPH ?taxonomyVersionGraph {
                                    ?targetTaxonomyGraph team:status ?status .
                                    ?targetTaxonomyGraph team:statusTransitionEvent ?statusTransitionEvent .
                                    ?targetTaxonomyGraph team:statusTransitionErrorMessage ?statusTransitionErrorMessage .
                                }
                            }
                            WHERE {
                                GRAPH ?taxonomyVersionGraph {
                                    ?targetTaxonomyGraph team:status ?status .
                                    OPTIONAL { ?targetTaxonomyGraph team:statusTransitionEvent ?statusTransitionEvent }
                                    OPTIONAL { ?targetTaxonomyGraph team:statusTransitionErrorMessage ?statusTransitionErrorMessage }
                                }
                            }
                            """);
                    delete.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
                    delete.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
                    delete.setBinding("status", preStatus);

                    Update insert = SPARQLFactory.updateWithConnection(
                            connection,
                            """
                            PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                            INSERT {
                                GRAPH ?taxonomyVersionGraph {
                                    ?targetTaxonomyGraph team:status ?updatedStatus .
                                    ?targetTaxonomyGraph team:statusTransitionEvent ?updatedStatusTransitionEvent .
                                }
                            }
                            WHERE {
                            }
                            """);
                    insert.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
                    insert.setBinding("targetTaxonomyGraph", targetTaxonomyGraph);
                    insert.setBinding("updatedStatusTransitionEvent", event);
                    insert.setBinding("updatedStatus", postStatus);

                    delete.execute();
                    insert.execute();

                    dispatcher.dispatch(eventModel);

                    connection.commit();
                } else {

                    log.warn("No target taxonomy graph in the precondition status {}", preStatus);
                }
            }
        }        
    }
}