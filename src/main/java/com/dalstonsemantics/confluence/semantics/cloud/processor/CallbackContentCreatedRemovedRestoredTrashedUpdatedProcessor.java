package com.dalstonsemantics.confluence.semantics.cloud.processor;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.PROV;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content;
import com.dalstonsemantics.confluence.semantics.cloud.domain.content.Contents;
import com.dalstonsemantics.confluence.semantics.cloud.domain.history.History;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.service.ContentService;
import com.dalstonsemantics.confluence.semantics.cloud.service.HistoryService;
import com.dalstonsemantics.confluence.semantics.cloud.util.ConfluenceResource;
import com.dalstonsemantics.confluence.semantics.cloud.util.ConfluenceResourceUrlParser;
import com.dalstonsemantics.confluence.semantics.cloud.util.ContentModel;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.util.TableIdMacroIdIRI;
import com.dalstonsemantics.confluence.semantics.cloud.util.TableIdMacroIdIRIValues;
import com.dalstonsemantics.confluence.semantics.cloud.util.XMLGregorianCalendarUtil;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;
import com.google.common.collect.Streams;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CallbackContentCreatedRemovedRestoredTrashedUpdatedProcessor extends AbstractContentEventProcessor {

    private AtlassianHostRepository atlassianHostRepository;
    private HistoryService historyService;
    private ContentService contentService;

    public CallbackContentCreatedRemovedRestoredTrashedUpdatedProcessor(
            @Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool,
            @Autowired AtlassianHostRepository atlassianHostRepository, 
            @Autowired HistoryService historyService,
            @Autowired ContentService contentService,
            @Autowired UUIDProvider uuidp,
            @Autowired EventDispatcher dispatcher,
            @Value("${addon.repositories.taxonomy.context.content.max-size}") long contentMaxSize) {
        super(taxonomyRepositoryPool, uuidp, dispatcher, contentMaxSize);
        this.atlassianHostRepository = atlassianHostRepository;
        this.historyService = historyService;
        this.contentService = contentService;
    }

    @Override
    protected IRI getEventType() {
        return TEAM.CALLBACK_CONTENT_CREATED_REMOVED_RESTORED_TRASHED_UPDATED_EVENT;
    }

    @SneakyThrows
    protected void processEvent(RepositoryConnection connection, Model eventModel, 
        IRI event, Literal clientKey, IRI contentGraph) {

        String eventClientKey = clientKey.stringValue();

        String eventUserAccountId = ((Literal)eventModel.getStatements(event, TEAM.ACCOUNT_ID, null).iterator().next().getObject()).stringValue();
        
        String eventContentId = ((Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_ID, null).iterator().next().getObject()).stringValue();
        String eventContentSpaceKey = ((Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_SPACE_KEY, null).iterator().next().getObject()).stringValue();
        String eventContentTitle = ((Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_TITLE, null).iterator().next().getObject()).stringValue();
        String eventContentType = ((Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_TYPE, null).iterator().next().getObject()).stringValue();
        int eventContentVersion = ((Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_VERSION, null).iterator().next().getObject()).intValue();
        String eventContentSelf = ((Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_SELF, null).iterator().next().getObject()).stringValue();
        Literal status = (Literal)eventModel.getStatements(event, TEAM.STATUS, null).iterator().next().getObject();

        createdRemovedRestoredTrashedUpdated(
            connection, contentGraph,
            eventClientKey, eventUserAccountId, 
            eventContentId, eventContentSpaceKey, eventContentTitle, eventContentType, eventContentVersion, eventContentSelf,
            status);
    }

    protected void createdRemovedRestoredTrashedUpdated(
            RepositoryConnection connection, IRI contentGraph,
            String eventClientKey, String eventUserAccountId, 
            String eventContentId, String eventContentSpaceKey, String eventContentTitle, String eventContentType, int eventContentVersion, String eventContentSelf,
            Literal status) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        ValueFactory vf = connection.getValueFactory();

        IRI contentS = vf.createIRI(Namespaces.CONTENT, String.format("%s-%s", eventClientKey, eventContentId));

        TupleQuery selectContent = SPARQLFactory.tupleQueryWithConnection(
                connection,
                """
                PREFIX prov: <http://www.w3.org/ns/prov#>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                SELECT ?contentId ?contentVersion ?status
                WHERE {
                    GRAPH ?contentGraph {
                    ?contentS team:contentId ?contentId ;
                        team:contentVersion ?contentVersion ;
                        team:status ?status .
                    }
                }
                """);
        selectContent.setBinding("contentGraph", contentGraph);
        selectContent.setBinding("contentS", contentS);

        Update deleteContent = SPARQLFactory.updateWithConnection(
                connection, 
                """
                PREFIX dcterms: <http://purl.org/dc/terms/>
                PREFIX prov: <http://www.w3.org/ns/prov#>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                DELETE {
                    GRAPH ?contentGraph {
                        ?contentS ?contentP ?contentO .
                    }
                }
                WHERE {
                    GRAPH ?contentGraph {
                        ?contentS ?contentP ?contentO .
                    }
                }
                """);
        deleteContent.setBinding("contentGraph", contentGraph);
        deleteContent.setBinding("contentS", contentS);

        Literal contentId = vf.createLiteral(eventContentId);

        Update deleteContentStatements = SPARQLFactory.updateWithConnection(
                connection,
                """
                    PREFIX dcterms: <http://purl.org/dc/terms/>
                    PREFIX prov: <http://www.w3.org/ns/prov#>
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    DELETE {
                        GRAPH ?contentGraph {
                            ?statementS ?statementP ?statementO .
                            ?activityS ?activityP ?activityO .
                            ?valueS ?valuesP ?valuesO .
                        }
                    }
                    WHERE {
                        GRAPH ?contentGraph {
                            ?valueS ?valuesP ?valuesO .
                            ?activityS ?activityP ?activityO .
                            ?statementS ?statementP ?statementO . 
                            ?activityS a prov:Activity ;
                            prov:generated ?statementS ;
                            prov:used ?valueS .
                            ?statementS a rdf:Statement ;
                                team:contentId ?contentId .
                        }
                    }
                """);
        deleteContentStatements.setBinding("contentGraph", contentGraph);
        deleteContentStatements.setBinding("contentId", contentId);

        IRI contentClass = vf.createIRI(TEAM.NAMESPACE, eventContentType);
        Literal contentVersion = vf.createLiteral(eventContentVersion);
        Literal title = vf.createLiteral(eventContentTitle);
        IRI source = vf.createIRI(eventContentSelf);

        Update updateContent = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX dcterms: <http://purl.org/dc/terms/>
                PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                INSERT {
                    GRAPH ?contentGraph {
                        ?contentS a ?contentClass ;
                            team:contentId ?contentId ;
                            team:contentVersion ?contentVersion ;
                            team:status ?status ;
                            dcterms:title ?title ;
                            dcterms:source ?source .
                    }
                }
                WHERE { }
                """);
        updateContent.setBinding("contentGraph", contentGraph);
        updateContent.setBinding("contentS", contentS);
        updateContent.setBinding("contentClass", contentClass);
        updateContent.setBinding("contentId", contentId);
        updateContent.setBinding("contentVersion", contentVersion);
        updateContent.setBinding("status", status);
        updateContent.setBinding("title", title);
        updateContent.setBinding("source", source);

        connection.setIsolationLevel(IsolationLevels.READ_UNCOMMITTED);

        connection.begin();

        try (TupleQueryResult result = selectContent.evaluate()) {

            int recordedContentVersion = Integer.MIN_VALUE;
            String recordedStatus = null;
            if (result.hasNext()) {

                BindingSet bindingSet = result.next();
                recordedContentVersion = Literals.getIntValue(bindingSet.getBinding("contentVersion").getValue(), Integer.MIN_VALUE);
                recordedStatus = bindingSet.getBinding("status").getValue().stringValue();

                if (!TEAM.REMOVED_STRING.equals(recordedStatus) && recordedContentVersion <= eventContentVersion) {

                    deleteContent.execute();
                    updateContent.execute();
                    if (TEAM.CURRENT_STRING.equals(status.stringValue())) {
                        AtlassianHost host = atlassianHostRepository.findById(eventClientKey).get();
                        History history = historyService.getHistory(host, eventContentId, eventContentVersion);
                        ContentModel contentModel = buildModelFromContent(host, vf, 
                                contentGraph,
                                eventClientKey, eventUserAccountId, 
                                eventContentId, eventContentSpaceKey, eventContentTitle, eventContentType, eventContentVersion,
                                history);
                        deleteContentStatements.execute();
                        for (IRI contentIRI : contentModel.getContentIRIs()) {
                            deleteContent.setBinding("contentS", contentIRI);
                            deleteContent.execute();
                        }
                        connection.add(contentModel.getStatements());
                        connection.add(contentModel.getContent());
                        connection.add(contentModel.getAgent());
                    }
                } else {    

                    log.info("Blog or Page stale, skipping. Recorded content status: {}. Recorded content version: {}. Version in the event: {}", 
                        recordedStatus, recordedContentVersion, eventContentVersion);
                }
            } else if (TEAM.CURRENT_STRING.equals(status.stringValue())) {

                updateContent.execute();
                AtlassianHost host = atlassianHostRepository.findById(eventClientKey).get();
                History history = historyService.getHistory(host, eventContentId, eventContentVersion);
                ContentModel contentModel = buildModelFromContent(host, vf, 
                        contentGraph,
                        eventClientKey, eventUserAccountId, 
                        eventContentId, eventContentSpaceKey, eventContentTitle, eventContentType, eventContentVersion,
                        history);
                deleteContentStatements.execute();
                connection.add(contentModel.getStatements());
                for (IRI contentIRI : contentModel.getContentIRIs()) {
                    deleteContent.setBinding("contentS", contentIRI);
                    deleteContent.execute();
                }
                connection.add(contentModel.getContent());
                connection.add(contentModel.getAgent());
            } else {

                log.info("First recoded event is not for the content with current status. Version in the event: {}", eventContentVersion);
            }
        }

        dispatchMaterializeContentGraphEvent(vf, eventClientKey);

        connection.commit();
    }    

    @SneakyThrows
    public ContentModel buildModelFromContent(
            AtlassianHost host, ValueFactory vf, 
            IRI contentGraph,
            String eventClientKey, String eventUserAccountId, 
            String eventContentId, String eventContentSpaceKey, String eventContentTitle, String eventContentType, int eventContentVersion,
            History history) {

        IRI contentS = vf.createIRI(Namespaces.CONTENT, String.format("%s-%s", eventClientKey, eventContentId));
        
        IRI agent = vf.createIRI(Namespaces.AGENT, eventUserAccountId);
        IRI activity = vf.createIRI(Namespaces.ACTIVITY, uuidp.randomUUID().toString());
        Literal timestamp = vf.createLiteral(XMLGregorianCalendarUtil.fromCalendar(history.getWhen()));

        ModelBuilder statementsModelBuilder = new ModelBuilder();
        statementsModelBuilder.setNamespace(TEAM.PREFIX, TEAM.NAMESPACE)
            .setNamespace(PROV.NS);

        statementsModelBuilder.namedGraph(contentGraph)
            .subject(agent)
                .add(RDF.TYPE, PROV.AGENT)
                .add(TEAM.ACCOUNT_ID, eventUserAccountId);

        statementsModelBuilder.namedGraph(contentGraph)
            .subject(activity)
                .add(RDF.TYPE, PROV.ACTIVITY)
                .add(PROV.STARTED_AT_TIME, timestamp)
                .add(PROV.ENDED_AT_TIME, timestamp)
                .add(PROV.WAS_ASSOCIATED_WITH, agent);

        List<IRI> contentIRIs = new LinkedList<>();

        ModelBuilder contentModelBuilder = new ModelBuilder();
        contentModelBuilder.setNamespace(TEAM.PREFIX, TEAM.NAMESPACE);

        List<IRI> agentIRIs = new LinkedList<>();

        ModelBuilder agentModelBuilder = new ModelBuilder();
        agentModelBuilder.setNamespace(TEAM.PREFIX, TEAM.NAMESPACE);

        // Use base of contentS to simplify retrieval of relative URIs
        Document document = Jsoup.parse(history.getContent().getBody().getStorage().getValue(), contentS.stringValue());

        populateDCTermsRelationStatements(document, 
                statementsModelBuilder, 
                vf, contentGraph, contentS, activity, eventContentId);

        Set<String> tableLocalIds = new HashSet<>();

        populateStatementsFromTableHeaderAsProperty(document, tableLocalIds, 
                statementsModelBuilder, contentIRIs, contentModelBuilder, agentIRIs, agentModelBuilder,
                host, vf, contentGraph, contentS, activity, eventClientKey, eventContentId, eventContentSpaceKey, eventContentTitle, eventContentType, eventContentVersion);
        populateStatementsFromTableFirstColumnAsProperty(document, tableLocalIds,
                statementsModelBuilder, contentIRIs, contentModelBuilder, agentIRIs, agentModelBuilder,
                host, vf, contentGraph, contentS, activity, eventClientKey, eventContentId, eventContentSpaceKey, eventContentTitle, eventContentType, eventContentVersion);

        log.info("Processed table local ids: {}.", tableLocalIds);

        ContentModel contentModel = ContentModel.builder()
            .statements(statementsModelBuilder.build())
            .contentIRIs(contentIRIs)
            .content(contentModelBuilder.build())
            .agentIRIs(agentIRIs)
            .agent(agentModelBuilder.build())
            .build();

        log.info("Built from content {} model {}.", eventContentId, contentModel);

        return contentModel;
    }

    private void populateDCTermsRelationStatements(Document document, ModelBuilder statementsModelBuilder, ValueFactory vf, IRI contentGraph, IRI content, IRI activity, String eventContentId) {

        Elements structuredMacro = document.select("ac|structured-macro[ac:name=taxonomies-for-confluence-relation]");
        for (Element element : structuredMacro) {
            String macroId = element.attr("ac:macro-id");
            Element acParameterUri = element.getElementsByAttributeValue("ac:name", "uri").first();
            Element acParameterUriUrl = acParameterUri.getElementsByTag("ri:url").first();
            String acParameterUriUrlValue = acParameterUriUrl.attr("ri:value");

            IRI macro = vf.createIRI(Namespaces.MACRO, macroId);
            IRI relation = vf.createIRI(acParameterUriUrlValue);

            statementsModelBuilder.namedGraph(contentGraph)
                .subject(macro)
                    .add(RDF.TYPE, RDF.STATEMENT)
                    .add(RDF.SUBJECT, content)
                    .add(RDF.PREDICATE, DCTERMS.RELATION)
                    .add(RDF.OBJECT, relation)
                    .add(TEAM.STATUS, TEAM.CURRENT)
                    .add(TEAM.CONTENT_ID, eventContentId)
                    .add(TEAM.MACRO_ID, macroId);

            BNode valueS = vf.createBNode(uuidp.randomUUID().toString());

            statementsModelBuilder.namedGraph(contentGraph)
                .subject(activity)
                    .add(PROV.GENERATED, macro)
                    .add(PROV.USED, valueS);

            statementsModelBuilder.namedGraph(contentGraph)
                .subject(valueS)
                    .add(DCTERMS.RELATION, relation);
        }
    }

    private void populateStatementsFromTableHeaderAsProperty(        
            Document document, Set<String> tableLocalIds, 
            ModelBuilder statementsModelBuilder, 
            List<IRI> contentIRIs, ModelBuilder contentModelBuilder, 
            List<IRI> agentIRIs, ModelBuilder agentModelBuilder,
            AtlassianHost host, ValueFactory vf, 
            IRI contentGraph, IRI contentS, IRI activity, 
            String eventClientKey, 
            String eventContentId, String eventContentSpaceKey, String eventContentTitle, String eventContentType, int eventContentVersion) {

        // Reuse common provenance information across all statements
        BNode valueS = vf.createBNode(uuidp.randomUUID().toString());

        // We need to ensure the same number of elements in the predicates and the individual rows of the table, so we can
        // deal with the mismatches and custom identifiers
        Map<String, List<TableIdMacroIdIRI>> predicates = new HashMap<>();

        Elements tableHeaders = document.select("table[ac:local-id] > tbody > tr:eq(0)");
        for (int i = 0; i < tableHeaders.size(); i++) {

            Element tableHeader = tableHeaders.get(i);
            String tableId = tableHeader.parent().parent().attr("ac:local-id");
            Elements tableHeaderElements = tableHeader.select("th");

            List<TableIdMacroIdIRI> predicatesForTableHeaderElements = new LinkedList<>();
            for (int j = 0; j < tableHeaderElements.size(); j ++) {
                predicatesForTableHeaderElements.add(buildPredicateFromTableHeaderElement(vf, tableId, j, tableHeaderElements.get(j)));
            }

            // Only consider tables where at least one column is a predicate
            long nonIdentifierNonEmptySize = predicatesForTableHeaderElements.stream().filter(predicate -> !predicate.isEmpty() && !predicate.isIdentifier()).count();
            if (nonIdentifierNonEmptySize > 0) {
                predicates.put(tableId, predicatesForTableHeaderElements);
            }
        }

        log.info("Table headers and properties. Predicates to use: {}.", predicates);

        if (predicates.keySet().size() > 0) {

            tableLocalIds.addAll(predicates.keySet());

            Elements tableRows = document.select("table[ac:local-id] > tbody > tr:gt(0)");
            for (int i = 0; i < tableRows.size(); i++) {

                Element tableRow = tableRows.get(i);
                String tableId = tableRow.parent().parent().attr("ac:local-id");

                log.info("Table headers and properties. Processing element. Element {}. Table {}.", tableRow, tableId);

                if (predicates.containsKey(tableId)) {

                    List<TableIdMacroIdIRI> predicatesForTableId = predicates.get(tableId);
                    Elements tableDataElements = tableRow.select("td");

                    // For readability and to help with debugging have two separate steps:
                    // one for collecting the triples and another one for populating the ModelBuilder
                    List<TableIdMacroIdIRIValues> triples = Streams
                        .zip(
                            predicatesForTableId.stream(),
                            tableDataElements.stream(),
                            (predicate, element) -> TableIdMacroIdIRIValues.builder()
                                    .tableId(predicate.getTableId())
                                    .macroId(predicate.getMacroId())
                                    .predicate(predicate.getPredicate())
                                    .objects(buildValuesFromElement(contentIRIs, contentModelBuilder, agentIRIs, agentModelBuilder, host, vf, contentGraph, 
                                            eventClientKey, 
                                            eventContentId, eventContentSpaceKey, eventContentTitle, eventContentType, element))
                                    .identifier(predicate.isIdentifier())
                                    .empty(predicate.isEmpty())
                                    .build())
                        .collect(Collectors.toList());

                    log.info("Table headers and properties. Zipped tables, macros, predicates and objects. Triples {}.", triples);

                    // Each row represents a resource
                    // By default statements are about https://tfc.dalstonsemantics.com/resource/{eventClientKey}-{eventContentId}-{i}
                    IRI resource = vf.createIRI(Namespaces.RESOURCE, String.format("%s-%s-%d", eventClientKey, eventContentId, i));

                    // Need to create statement for each resource and value in the table
                    // Sart iterating over resources
                    for (int j = 0; j < triples.size(); j++) {

                        // Get values for each resource
                        TableIdMacroIdIRIValues triple = triples.get(j);

                        log.info("Table headers and properties. Processing triple {}.", triple);

                        // Reset resource identifier if first column header is a URI.
                        // Relying on existing logic to deal with relative URIs. They are in the content namespace https://tfc.dalstonsemantics.com/content/{eventClientKey}-{eventContentId}#{value of relative URI}
                        // Absolute URIs are kept as-is, so statements can be about anything
                        if (triple.isIdentifier() && triple.getObjects().size() > 0 && triple.getObjects().get(0).isIRI()) {
                            resource = (IRI)triple.getObjects().get(0);
                            log.info("Identifier column with URI. Resetting resource: {}", resource);
                            continue;
                        }

                        // Ignore values if there is no header
                        if (triple.isEmpty()) {
                            log.info("Empty predicate column. Skipping.");
                            continue;
                        }

                        IRI predicate = triple.getPredicate();
                        List<org.eclipse.rdf4j.model.Value> objects = triple.getObjects();

                        statementsModelBuilder.namedGraph(contentGraph)
                            .subject(activity)
                                .add(PROV.USED, valueS);    

                        statementsModelBuilder.namedGraph(contentGraph)
                            .subject(valueS)
                                .add(TEAM.CONTENT_ID, eventContentId)
                                .add(TEAM.CONTENT_VERSION, eventContentVersion);

                        // Iterate over values
                        // We need individual statement for each of the values
                        for (int k = 0; k < objects.size(); k++) {
                            
                            org.eclipse.rdf4j.model.Value object = objects.get(k);

                            log.info("Table headers and properties. Creating statements for Object: {}.", object);

                            // Add statement (statement identifier is different from resource identifier)
                            IRI statement = vf.createIRI(Namespaces.RESOURCE_TABLE, String.format("%s-%s-%d-%d-%d", eventClientKey, eventContentId, i, j, k));

                            statementsModelBuilder.namedGraph(contentGraph)
                                .subject(statement)
                                    .add(RDF.TYPE, RDF.STATEMENT)
                                    .add(RDF.SUBJECT, resource)
                                    .add(RDF.PREDICATE, predicate)
                                    .add(RDF.OBJECT, object)
                                    .add(TEAM.STATUS, TEAM.CURRENT)
                                    .add(TEAM.CONTENT, contentS)
                                    .add(TEAM.CONTENT_ID, eventContentId)
                                    .add(TEAM.TABLE_ID, triple.getTableId())
                                    .add(TEAM.MACRO_ID, triple.getMacroId());

                            statementsModelBuilder.namedGraph(contentGraph)
                                .subject(activity)
                                    .add(PROV.GENERATED, statement);

                            log.info("Table headers and properties. Statement created.");
                        }
                    }
                }
            }
        }
    }

    private void populateStatementsFromTableFirstColumnAsProperty(
            Document document, Set<String> tableLocalIds, 
            ModelBuilder statementsModelBuilder, 
            List<IRI> contentIRIs, ModelBuilder contentModelBuilder, 
            List<IRI> agentIRIs, ModelBuilder agentModelBuilder,
            AtlassianHost host, ValueFactory vf, 
            IRI contentGraph, IRI contentS, IRI activity, 
            String eventClientKey, 
            String eventContentId, String eventContentSpaceKey, String eventContentTitle, String eventContentType, int eventContentVersion) {

        // Reuse common provenance information across all statements
        BNode valueS = vf.createBNode(uuidp.randomUUID().toString());

        Set<String> tableIds = new HashSet<>();

        // Take all rows where first cell contains resource macro, only take first resource macro
        Elements macrosInFirstColumn = document.select("table[ac:local-id] > tbody > tr > td:eq(0) > p > ac|structured-macro[ac:name=taxonomies-for-confluence-resource]:eq(0) > ac|parameter[ac:name=uri] > ri|url[ri:value]");
        for (int i = 0; i < macrosInFirstColumn.size(); i ++ ) {

            Element element = macrosInFirstColumn.get(i);

            String tableId = element.parent().parent().parent().parent().parent().parent().parent().attr("ac:local-id");
            String macroId = element.parent().parent().attr("ac:macro-id");
            String uri = element.attr("ri:value");

            // Tables with header cells as predicates take precendnce
            if (!tableLocalIds.contains(tableId)) {

                // For all such columns take siblings
                Elements tableDataElements = element.parent().parent().parent().parent().siblingElements();
                for (int j = 0; j < tableDataElements.size(); j ++) {

                    Element tableDataElement = tableDataElements.get(j);

                    // Reuse predicate when creating statements
                    IRI predicate = vf.createIRI(uri);

                    statementsModelBuilder.namedGraph(contentGraph)
                        .subject(activity)
                            .add(PROV.USED, valueS);    

                    statementsModelBuilder.namedGraph(contentGraph)
                        .subject(valueS)
                            .add(TEAM.CONTENT_ID, eventContentId)
                            .add(TEAM.CONTENT_VERSION, eventContentVersion);

                    // Each value requires a separate statement
                    List<org.eclipse.rdf4j.model.Value> objects = buildValuesFromElement(contentIRIs, contentModelBuilder, agentIRIs, agentModelBuilder, host, vf, contentGraph, 
                            eventClientKey, 
                            eventContentId, eventContentSpaceKey, eventContentTitle, eventContentType, tableDataElement);
                    for (int k = 0; k < objects.size(); k ++) {

                        org.eclipse.rdf4j.model.Value object = objects.get(k);

                        // Add statement
                        IRI statement = vf.createIRI(Namespaces.TABLE, String.format("%s-%s-%d-%d-%d", eventClientKey, eventContentId, i, j, k));

                        // Statements are about https://tfc.dalstonsemantics.com/content/{eventClientKey}-{eventContentId}
                        statementsModelBuilder.namedGraph(contentGraph)
                            .subject(statement)
                                .add(RDF.TYPE, RDF.STATEMENT)
                                .add(RDF.SUBJECT, contentS)
                                .add(RDF.PREDICATE, predicate)
                                .add(RDF.OBJECT, object)
                                .add(TEAM.STATUS, TEAM.CURRENT)
                                .add(TEAM.CONTENT_ID, eventContentId)
                                .add(TEAM.TABLE_ID, tableId)
                                .add(TEAM.MACRO_ID, macroId);

                        statementsModelBuilder.namedGraph(contentGraph)
                            .subject(activity)
                                .add(PROV.GENERATED, statement);
                    }
                }

                // Maintain local list of table identifiers we are processing
                // So that it does not interfere with building the set of statements
                tableIds.add(tableId);
            }
        }

        // Add all table identifiers once processing is complete
        tableLocalIds.addAll(tableIds);
    }

    private TableIdMacroIdIRI buildPredicateFromTableHeaderElement(ValueFactory vf, String tableId, int countWithinTableHeader, Element tableHeaderElement) {

        Elements resourceMacroElements = tableHeaderElement.select("ac|structured-macro[ac:name=taxonomies-for-confluence-resource] > ac|parameter[ac:name=uri] > ri|url[ri:value]");
        if (resourceMacroElements.size() > 0) {
            
            String macroId = resourceMacroElements.get(0).parent().parent().attr("ac:macro-id");
            String uri = resourceMacroElements.get(0).attr("ri:value");
            return TableIdMacroIdIRI.builder().tableId(tableId).macroId(macroId).predicate(vf.createIRI(uri)).build();
        } else if (countWithinTableHeader == 0) {

            return TableIdMacroIdIRI.builder().tableId(tableId).identifier(true).build();
        }

        return TableIdMacroIdIRI.builder().tableId(tableId).empty(true).build();
    }

    private List<org.eclipse.rdf4j.model.Value> buildValuesFromElement(
        List<IRI> contentIRIs, ModelBuilder contentModelBuilder, 
        List<IRI> agentIRIs, ModelBuilder agentModelBuilder,
        AtlassianHost host, ValueFactory vf, IRI contentGraph, 
        String eventClientKey, 
        String eventContentId, String eventContentSpaceKey, String eventContentTitle, String eventContentType, Element element) {

        List<org.eclipse.rdf4j.model.Value> values = new LinkedList<>();

        // Resource macros
        Elements resourceMacroElements = element.select("ac|structured-macro[ac:name=taxonomies-for-confluence-resource] > ac|parameter[ac:name=uri] > ri|url[ri:value]");
        if (resourceMacroElements.size() > 0) {
            values.addAll(resourceMacroElements.stream().map(e -> {
                IRI value = vf.createIRI(e.attr("ri:value"));
                return value;
            }).collect(Collectors.toList()));
        } else {

            // Links to other pages, blog posts
            Elements riPageElements = element.select("ac|link > ri|page");
            Elements riBlogPostElements = element.select("ac|link > ri|blog-post");
            Elements acLinkBodyElements = element.select("ac|link > ac|link-body:eq(0)");
            if (riPageElements.size() > 0 || riBlogPostElements.size() > 0 || acLinkBodyElements.size() > 0) {
                values.addAll(riPageElements.stream().map(e -> {
                    String riSpaceKey = e.attr("ri:space-key");
                    String riContentTitle = e.attr("ri:content-title");

                    log.info("About to get content. RI Type: {}. RI Space Key: {}. Content Space Key: {}. RI Title: {}.", "page", riSpaceKey, eventContentSpaceKey, riContentTitle);

                    // TODO: Reconsider how we retrieve it, perhaps failing here is fine
                    Contents contents = contentService.getContents(host, "page", riSpaceKey.isEmpty() ? eventContentSpaceKey : riSpaceKey, riContentTitle);
                    Content content = contents.getResults().get(0);

                    log.info("Contents to use: {}.", contents);

                    IRI contentIRI = vf.createIRI(Namespaces.CONTENT, String.format("%s-%s", eventClientKey, content.getId()));
                    IRI contentClass = vf.createIRI(TEAM.NAMESPACE, content.getType());

                    // Complexity of Confluence API - when searching for content we don't get the base returned in the links; have to get from host
                    IRI source = vf.createIRI(String.format("%s%s", host.getBaseUrl(), content.getLinks().getWebui()));
                    contentIRIs.add(contentIRI);
                    contentModelBuilder.namedGraph(contentGraph)
                        .subject(contentIRI)
                            .add(RDF.TYPE, contentClass)
                            .add(TEAM.CONTENT_ID, content.getId())
                            .add(TEAM.CONTENT_VERSION, content.getVersion().getNumber())
                            .add(TEAM.STATUS, TEAM.CURRENT)
                            .add(DCTERMS.TITLE, content.getTitle())
                            .add(DCTERMS.SOURCE, source);
                    return contentIRI;
                }).collect(Collectors.toList()));
                values.addAll(riBlogPostElements.stream().map(e -> {
                    String riSpaceKey = e.attr("ri:space-key");
                    String riContentTitle = e.attr("ri:content-title");

                    log.info("About to get content. RI Type: {}. RI Space Key: {}. Content Space Key: {}. RI Title: {}.", "blogpost", riSpaceKey, eventContentSpaceKey, riContentTitle);

                    // TODO: Reconsider how we retrieve it, perhaps failing here is fine
                    Contents contents = contentService.getContents(host, "blogpost", riSpaceKey.isEmpty() ? eventContentSpaceKey : riSpaceKey, riContentTitle);
                    Content content = contents.getResults().get(0);
                    
                    log.info("Contents to use: {}.", contents);
                    
                    IRI contentIRI = vf.createIRI(Namespaces.CONTENT, String.format("%s-%s", eventClientKey, content.getId()));
                    IRI contentClass = vf.createIRI(TEAM.NAMESPACE, content.getType());

                    // Complexity of Confluence API - when searching for content we don't get the base returned in the links; have to get from host
                    IRI source = vf.createIRI(String.format("%s%s", host.getBaseUrl(), content.getLinks().getWebui()));
                    contentIRIs.add(contentIRI);
                    contentModelBuilder.namedGraph(contentGraph)
                        .subject(contentIRI)
                            .add(RDF.TYPE, contentClass)
                            .add(TEAM.CONTENT_ID, content.getId())
                            .add(TEAM.CONTENT_VERSION, content.getVersion().getNumber())
                            .add(TEAM.STATUS, TEAM.CURRENT)
                            .add(DCTERMS.TITLE, content.getTitle())
                            .add(DCTERMS.SOURCE, source);
                    return contentIRI;
                }).collect(Collectors.toList()));
                values.addAll(acLinkBodyElements.stream().map(e -> {
                    String acLinkBodytext = e.text();

                    log.info("About to see if we need to get content as opposed to using self. Content Type: {}. Content Space Key: {}. Content Title: {}. Link Table: {}.", 
                            eventContentType, eventContentSpaceKey, eventContentTitle, acLinkBodytext);

                    if (eventContentTitle.equals(acLinkBodytext)) {

                        log.info("Using self. Content Id: {}.", eventContentId);

                        IRI contentIRI = vf.createIRI(Namespaces.CONTENT, String.format("%s-%s", eventClientKey, eventContentId));
                        return contentIRI;
                    } else {

                        // TODO: Reconsider how we retrieve it, perhaps failing here is fine
                        Contents contents = contentService.getContents(host, eventContentType, eventContentSpaceKey, acLinkBodytext);
                        Content content = contents.getResults().get(0);

                        log.info("Contents to use: {}.", contents);

                        IRI contentIRI = vf.createIRI(Namespaces.CONTENT, String.format("%s-%s", eventClientKey, content.getId()));
                        IRI contentClass = vf.createIRI(TEAM.NAMESPACE, content.getType());

                        // Complexity of Confluence API - when searching for content we don't get the base returned in the links; have to get from host
                        IRI source = vf.createIRI(String.format("%s%s", host.getBaseUrl(), content.getLinks().getWebui()));
                        contentIRIs.add(contentIRI);
                        contentModelBuilder.namedGraph(contentGraph)
                            .subject(contentIRI)
                                .add(RDF.TYPE, contentClass)
                                .add(TEAM.CONTENT_ID, content.getId())
                                .add(TEAM.CONTENT_VERSION, content.getVersion().getNumber())
                                .add(TEAM.STATUS, TEAM.CURRENT)
                                .add(DCTERMS.TITLE, content.getTitle())
                                .add(DCTERMS.SOURCE, source);
                        return contentIRI;
                    }
                }).collect(Collectors.toList()));
            } else {

                // Links with "simple" href (links to other pages with anchors are simple links)
                Elements aElements = element.select("a[href]");
                if (aElements.size() > 0) {

                    // Process
                    values.addAll(aElements.stream().map(aElement -> {

                        // Absolute URL will return url along with the correct refix for the content
                        String absHref = aElement.attr("abs:href");

                        if (absHref.startsWith(host.getBaseUrl())) {

                            // Parse URL that is within the host base
                            ConfluenceResource confluenceResource = ConfluenceResourceUrlParser.parseUrl(absHref);

                            if (confluenceResource.getAnchor() == null) {

                                // This is simply a page link as opposed to link to a stable resource. Stay consistent and create the content record

                                log.info("Href that is within the host base without an anchor. Href: {}. Parsed: {}.", absHref, confluenceResource);

                                // TODO: Reconsider how we retrieve it, perhaps failing here is fine for now
                                Content content = contentService.getContentById(host, confluenceResource.getContentId());
            
                                log.info("Content to use: {}.", content);

                                IRI contentIRI = vf.createIRI(Namespaces.CONTENT, String.format("%s-%s", eventClientKey, content.getId()));
                                IRI contentClass = vf.createIRI(TEAM.NAMESPACE, content.getType());
            
                                IRI source = vf.createIRI(String.format("%s%s", host.getBaseUrl(), content.getLinks().getWebui()));
                                contentIRIs.add(contentIRI);
                                contentModelBuilder.namedGraph(contentGraph)
                                    .subject(contentIRI)
                                        .add(RDF.TYPE, contentClass)
                                        .add(TEAM.CONTENT_ID, content.getId())
                                        .add(TEAM.CONTENT_VERSION, content.getVersion().getNumber())
                                        .add(TEAM.STATUS, TEAM.CURRENT)
                                        .add(DCTERMS.TITLE, content.getTitle())
                                        .add(DCTERMS.SOURCE, source);
                                return contentIRI;
                            } else {

                                log.info("Href that is within the host base with anchor. Href: {}. Parsed: {}.", absHref, confluenceResource);

                                // ... but given an anchor we can just assume this has been created by the user
                                IRI contentResourceIRI = vf.createIRI(Namespaces.CONTENT, String.format("%s-%s#%s", eventClientKey, confluenceResource.getContentId(), confluenceResource.getAnchor()));
                                return contentResourceIRI;
                            }
                        }

                        log.info("Href that is outside the host base. Href: {}.", absHref);

                        // Any other URI, including relative
                        IRI value = vf.createIRI(absHref);
                        return value;
                    }).collect(Collectors.toList()));
                } else {

                    // Links to users
                    Elements riUserElements = element.select("ac|link > ri|user[ri:account-id]");
                    if (riUserElements.size() > 0) {
                        values.addAll(riUserElements.stream().map(e -> {
                            String riAccountId = e.attr("ri:account-id");
                            IRI agentIRI = vf.createIRI(Namespaces.AGENT, riAccountId);
                            agentIRIs.add(agentIRI);
                            agentModelBuilder.namedGraph(contentGraph)
                                .subject(agentIRI)
                                    .add(RDF.TYPE, PROV.AGENT)
                                    .add(TEAM.ACCOUNT_ID, riAccountId);
                            return agentIRI;
                        }).collect(Collectors.toList()));
                    } else {

                        // Timestamps
                        Elements timeElements = element.select("time[datetime]");
                        if (timeElements.size() > 0) {
                            values.addAll(timeElements.stream().map(e -> {
                                try {
                                    Literal value = vf.createLiteral(XMLGregorianCalendarUtil.fromConfluenceDateTime(e.attr("datetime")));
                                    return value;
                                } catch (DatatypeConfigurationException dce) {
                                    Literal value = vf.createLiteral(e.attr("datetime"));
                                    return value;
                                }
                            }).collect(Collectors.toList()));
                        } else {

                            // Any other text
                            String text = element.text();  
                            if (!text.isBlank()) {
                                Literal value = vf.createLiteral(text);
                                values.add(value);
                            }
                        }
                    }
                }
            }
        }
        return values;
    }

}
