package com.dalstonsemantics.confluence.semantics.cloud.processor;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CallbackPropertyRemovedProcessor extends AbstractContentEventProcessor {

    public CallbackPropertyRemovedProcessor(
            @Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool,
            @Autowired UUIDProvider uuidp,
            @Autowired EventDispatcher dispatcher,
            @Value("${addon.repositories.taxonomy.context.content.max-size}") long contentMaxSize) {
        super(taxonomyRepositoryPool, uuidp, dispatcher, contentMaxSize);
    }

    @Override
    protected IRI getEventType() {
        return TEAM.CALLBACK_PROPERTY_REMOVED_EVENT;
    }

    @SneakyThrows
    protected void processEvent(RepositoryConnection connection, Model eventModel, 
            IRI event, Literal clientKey, IRI contentGraph) {

        String eventClientKey = clientKey.stringValue();
    
        String eventUserAccountId = ((Literal)eventModel.getStatements(event, TEAM.ACCOUNT_ID, null).iterator().next().getObject()).stringValue();

        String eventContentId = ((Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_ID, null).iterator().next().getObject()).stringValue();
        String eventContentTitle = ((Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_TITLE, null).iterator().next().getObject()).stringValue();
        int eventContentVersion = ((Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_VERSION, null).iterator().next().getObject()).intValue();
        Literal eventContentWhen = (Literal)eventModel.getStatements(event, TEAM.EVENT_CONTENT_WHEN, null).iterator().next().getObject();

        propertyRemoved(connection, contentGraph,
                eventClientKey, eventUserAccountId,
                eventContentId, eventContentTitle, eventContentVersion, eventContentWhen);
    }

    public void propertyRemoved(
            RepositoryConnection connection, IRI contentGraph,
            String eventClientKey, String eventUserAccountId,
            String eventContentId, String eventContentTitle, int eventContentVersion, Literal eventContentWhen) 
            throws IOException, NoSuchAlgorithmException, DatatypeConfigurationException {

        ValueFactory vf = connection.getValueFactory();

        IRI statementS = vf.createIRI(Namespaces.PROPERTY, String.format("%s-%s", eventClientKey, eventContentId));
        
        // We are not the master of property, content or agent information, Confluence stores the versions
        // and the history. We can recover missing properties, contents and agents from Confluence, not vice-versa,
        // therefore only store last activity, not full history.

        // Consider Subject or Type property as rdf:Statement about a piece of content. Use propertyVersion as
        // part of the statement to validate updates.

        // Only consider dcterms:subject and dcterms:type properties of content as materialization of the Statement.
        // Content conflicts can be resolved via page events.

        Update deleteProperty = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX dcterms: <http://purl.org/dc/terms/>
                PREFIX prov: <http://www.w3.org/ns/prov#>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                DELETE {
                    GRAPH ?contentGraph {
                        ?statementS ?statementP ?statementO .
                        ?activityS ?activityP ?activityO .
                        ?valueS ?valueP ?valueO .
                    }
                }
                WHERE {
                    GRAPH ?contentGraph {
                        ?valueS ?valueP ?valueO .
                        ?activityS ?activityP ?activityO .
                        ?statementS ?statementP ?statementO .
                        ?activityS a prov:Activity ;
                            prov:generated ?statementS ;
                            prov:used ?valueS .
                    }
                }
                """);
        deleteProperty.setBinding("contentGraph", contentGraph);
        deleteProperty.setBinding("statementS", statementS);

        Literal propertyId = vf.createLiteral(eventContentId);
        Literal propertyVersion = vf.createLiteral(eventContentVersion);
                
        Update updateProperty = SPARQLFactory.updateWithConnection(
                connection,
                """
                PREFIX dcterms: <http://purl.org/dc/terms/>
                PREFIX prov: <http://www.w3.org/ns/prov#>
                PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                INSERT {
                    GRAPH ?contentGraph {
                        ?statementS a rdf:Statement ;
                            team:propertyId ?propertyId ;
                            team:propertyVersion ?propertyVersion ;
                            team:status ?status .
                        ?agentS a prov:Agent ;
                            team:accountId ?accountId .
                        ?activityS a prov:Activity ;
                            prov:startedAtTime ?timestamp ;
                            prov:endedAtTime ?timestamp ;
                            prov:wasAssociatedWith ?agentS ;
                            prov:invalidated ?statementS .
                    }
                }
                WHERE { }
                """);
        updateProperty.setBinding("contentGraph", contentGraph);
        updateProperty.setBinding("statementS", statementS);
        updateProperty.setBinding("propertyId", propertyId);
        updateProperty.setBinding("propertyVersion", propertyVersion);
        updateProperty.setBinding("status", TEAM.REMOVED);

        IRI agentS = vf.createIRI(Namespaces.AGENT, eventUserAccountId);
        Literal accountId = vf.createLiteral(eventUserAccountId);
        
        updateProperty.setBinding("agentS", agentS);
        updateProperty.setBinding("accountId", accountId);

        IRI activityS = vf.createIRI(Namespaces.ACTIVITY, uuidp.randomUUID().toString());

        updateProperty.setBinding("activityS", activityS);
        updateProperty.setBinding("timestamp", eventContentWhen);

        BNode valueS = vf.createBNode(uuidp.randomUUID().toString());

        updateProperty.setBinding("valueS", valueS);

        connection.setIsolationLevel(IsolationLevels.READ_UNCOMMITTED);

        connection.begin();

        deleteProperty.execute();
        updateProperty.execute();

        dispatchMaterializeContentGraphEvent(vf, eventClientKey);

        connection.commit();
    }
}
