package com.dalstonsemantics.confluence.semantics.cloud.listener;

import com.atlassian.connect.spring.AtlassianHost;
import com.dalstonsemantics.confluence.semantics.cloud.domain.user.User;
import com.dalstonsemantics.confluence.semantics.cloud.provider.LocalDateTimeProvider;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.service.UserService;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.util.XMLGregorianCalendarUtil;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TaxonomyVersionGraphInitializer {
    
    private Rdf4jRepositoryPool taxonomyRepositoryPool;
    private LocalDateTimeProvider ldtp;
    private UUIDProvider uuidp;
    private UserService userService;

    public TaxonomyVersionGraphInitializer(@Autowired @Qualifier("TaxonomyRepositoryPool") Rdf4jRepositoryPool taxonomyRepositoryPool,
            @Autowired LocalDateTimeProvider ldtp,
            @Autowired UUIDProvider uuidp,
            @Autowired UserService userService) {
        this.taxonomyRepositoryPool = taxonomyRepositoryPool;
        this.ldtp = ldtp;
        this.uuidp = uuidp;
        this.userService = userService;
    }

    @SneakyThrows
    public void initializeTaxonomyVersionGraph(AtlassianHost host) {

        log.info("Creating initial taxonomy versions");

        String clientKey = host.getClientKey();

        Repository taxonomyRepository = taxonomyRepositoryPool.getRepository(clientKey);

        try (RepositoryConnection connection = taxonomyRepository.getConnection()) {

            connection.setIsolationLevel(IsolationLevels.READ_UNCOMMITTED);

            ValueFactory vf = taxonomyRepository.getValueFactory();
            IRI taxonomyVersionGraph = vf.createIRI(Namespaces.TAXONOMY_VERSION_GRAPH, clientKey);

            if (connection.size(taxonomyVersionGraph) == 0) {

                User user = userService.getCurrentUser(host);
                String accountId = user.getAccountId();        

                IRI versionZero = vf.createIRI(Namespaces.TAXONOMY_GRAPH, clientKey);
                IRI versionOne = vf.createIRI(Namespaces.TAXONOMY_GRAPH, "%s-%s".formatted(clientKey, uuidp.randomUUID().toString()));
                IRI agent = vf.createIRI(Namespaces.AGENT, accountId);
                IRI activity = vf.createIRI(Namespaces.ACTIVITY, uuidp.randomUUID().toString());
                Literal timestamp = vf.createLiteral(XMLGregorianCalendarUtil.fromLocalDateTime(ldtp.nowInUTC()));

                Update insert = SPARQLFactory.updateWithConnection(
                        connection,
                        """
                        PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                        PREFIX prov: <http://www.w3.org/ns/prov#>
                        INSERT {
                            GRAPH ?taxonomyVersionGraph {
                                ?versionZero a team:TaxonomyGraph ;
                                    team:status ?versionZeroStatus ;
                                    team:conceptCount 0 ;
                                    team:classCount 0 ;
                                    team:propertyCount 0 ;
                                    team:insertedConceptCount 0 ;
                                    team:updatedConceptCount 0 ;
                                    team:deletedConceptCount 0 ;
                                    team:insertedClassCount 0 ;
                                    team:updatedClassCount 0 ;
                                    team:deletedClassCount 0 ;
                                    team:insertedPropertyCount 0 ;
                                    team:updatedPropertyCount 0 ;
                                    team:deletedPropertyCount 0 ;
                                    team:impactedContentCount 0 ;
                                    team:processedContentCount 0 ;
                                    team:failedContentCount 0 .
                                ?versionOne a team:TaxonomyGraph ;
                                    team:previousTaxonomyGraph ?versionZero ;
                                    team:status ?versionOneStatus ;
                                    team:conceptCount 0 ;
                                    team:classCount 0 ;
                                    team:propertyCount 0 ;
                                    team:insertedConceptCount 0 ;
                                    team:updatedConceptCount 0 ;
                                    team:deletedConceptCount 0 ;
                                    team:insertedClassCount 0 ;
                                    team:updatedClassCount 0 ;
                                    team:deletedClassCount 0 ;
                                    team:insertedPropertyCount 0 ;
                                    team:updatedPropertyCount 0 ;
                                    team:deletedPropertyCount 0 .
                                ?agent a prov:Agent ;
                                    team:accountId ?accountId .
                                ?activity a prov:Activity ;
                                    prov:startedAtTime ?timestamp ;
                                    prov:endedAtTime ?timestamp ;
                                    prov:wasAssociatedWith ?agent ;
                                    prov:generated ?versionZero ;
                                    prov:generated ?versionOne .
                            }
                        }
                        WHERE {
                        }
                        """);
                insert.setBinding("taxonomyVersionGraph", taxonomyVersionGraph);
                insert.setBinding("versionZero", versionZero);
                insert.setBinding("versionOne", versionOne);
                insert.setBinding("versionZeroStatus", TEAM.CURRENT);
                insert.setBinding("versionOneStatus", TEAM.DRAFT);
                insert.setBinding("agent", agent);
                insert.setBinding("accountId", vf.createLiteral(accountId));
                insert.setBinding("activity", activity);
                insert.setBinding("timestamp", timestamp);

                connection.begin();
                
                insert.execute();

                connection.commit();

                log.info("Completed creation of initial taxonomy versions.");
            } else {

                log.info("Data for context {} already exists.", taxonomyVersionGraph);
            }
        }
    }
}
