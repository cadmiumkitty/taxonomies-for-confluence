package com.dalstonsemantics.confluence.semantics.cloud.host;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.dalstonsemantics.confluence.semantics.cloud.cache.CacheConfig;
import com.dalstonsemantics.confluence.semantics.cloud.provider.LocalDateTimeProvider;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;
import com.dalstonsemantics.confluence.semantics.cloud.util.SPARQLFactory;
import com.dalstonsemantics.confluence.semantics.cloud.util.XMLGregorianCalendarUtil;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.Namespaces;

import org.eclipse.rdf4j.common.transaction.IsolationLevels;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Rdf4JAtlassianHostRepository implements AtlassianHostRepository {

    private static final String HOST_REPOSITORY_ID = "host";

    private Rdf4jRepositoryPool hostRepositoryPool;
    private LocalDateTimeProvider ldtp;
    private UUIDProvider uuidp;
    private Cache<String, AtlassianHost> hostCache;
    private String hostSuffix;
    private SharedSecretEncryptorDecryptor sharedSecretEncryptorDecryptor;
    private String keyIdentifier;

    public Rdf4JAtlassianHostRepository(
            @Autowired @Qualifier("HostRepositoryPool") Rdf4jRepositoryPool hostRepositoryPool,
            @Autowired LocalDateTimeProvider ldtp, 
            @Autowired UUIDProvider uuidp,
            @Autowired @Qualifier(CacheConfig.HOST_CACHE_NAME) Cache<String, AtlassianHost> hostCache,
            @Value("${addon.host.validation.suffix}") String hostSuffix,
            @Autowired SharedSecretEncryptorDecryptor sharedSecretEncryptorDecryptor,
            @Value("${addon.host.shared-secret.key-identifier}") String keyIdentifier) {
        this.hostRepositoryPool = hostRepositoryPool;
        this.ldtp = ldtp;
        this.uuidp = uuidp;
        this.hostCache = hostCache;
        this.hostSuffix = hostSuffix;
        this.sharedSecretEncryptorDecryptor = sharedSecretEncryptorDecryptor;
        this.keyIdentifier = keyIdentifier;
    }

    @Override
    @SneakyThrows
    public AtlassianHost save(AtlassianHost atlassianHost) {

        log.info("Calling save for AtlassianHost: {}", atlassianHost);

        try {
            URL atlassianHostBaseUrl = new URL(atlassianHost.getBaseUrl());
            if (!atlassianHostBaseUrl.getHost().endsWith(hostSuffix)) {
                throw new IllegalArgumentException("Rogue host name in base URL in AtlassianHost.");
            }
        } catch (MalformedURLException murle) {
            throw new IllegalArgumentException("Rogue base URL in AtlassianHost.", murle);
        }

        String sharedSecretCyphertextBase64 = sharedSecretEncryptorDecryptor.encrypt(atlassianHost.getSharedSecret(), keyIdentifier);

        Repository hostRepository = hostRepositoryPool.getRepository(HOST_REPOSITORY_ID);

        try (RepositoryConnection connection = hostRepository.getConnection()) {

            connection.setIsolationLevel(IsolationLevels.READ_UNCOMMITTED);

            ValueFactory vf = hostRepository.getValueFactory();
            IRI s = vf.createIRI(buildClientKeyIRIString(atlassianHost.getClientKey()));

            IRI activity = vf.createIRI(Namespaces.ACTIVITY, uuidp.randomUUID().toString());
            Literal timestamp = vf.createLiteral(XMLGregorianCalendarUtil.fromLocalDateTime(ldtp.nowInUTC()));
            BNode value = vf.createBNode(uuidp.randomUUID().toString());

            Update delete = SPARQLFactory.updateWithConnection(
                    connection, 
                    """
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    PREFIX prov: <http://www.w3.org/ns/prov#>
                    DELETE { 
                        ?s ?p ?o .
                    }
                    WHERE { 
                        ?s ?p ?o .
                    }
                    """);
            delete.setBinding("s", s);

            Update insert = SPARQLFactory.updateWithConnection(
                    connection, 
                    """
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    PREFIX prov: <http://www.w3.org/ns/prov#>
                    INSERT {
                        ?s a team:AtlassianHost ;
                            team:baseUrl ?baseUrl ;
                            team:clientKey ?clientKey ;
                            team:description ?description ;
                            team:productType ?productType ;
                            team:sharedSecret ?sharedSecret ;
                            team:keyIdentifier ?keyIdentifier ;
                            team:addonInstalled ?addonInstalled ;
                            team:oauthClientId ?oauthClientId ;
                            team:serviceEntitlementNumber ?serviceEntitlementNumber .
                        ?activity a prov:Activity ;
                            prov:startedAtTime ?timestamp ;
                            prov:endedAtTime ?timestamp ;
                            prov:generated ?s ;
                            prov:used ?value .
                        ?value team:addonInstalled ?addonInstalled ;
                            team:sharedSecret ?sharedSecret ; 
                            team:keyIdentifier ?keyIdentifier .
                    }
                    WHERE {
                    }
                    """);
            insert.setBinding("s", s);
            insert.setBinding("baseUrl", vf.createLiteral(atlassianHost.getBaseUrl()));
            insert.setBinding("clientKey", vf.createLiteral(atlassianHost.getClientKey()));
            insert.setBinding("description", vf.createLiteral(atlassianHost.getDescription()));
            insert.setBinding("productType", vf.createLiteral(atlassianHost.getProductType()));
            insert.setBinding("sharedSecret", vf.createLiteral(sharedSecretCyphertextBase64));
            insert.setBinding("keyIdentifier", vf.createLiteral(keyIdentifier));
            insert.setBinding("addonInstalled", vf.createLiteral(atlassianHost.isAddonInstalled()));

            if (atlassianHost.getOauthClientId() != null) {
                insert.setBinding("oauthClientId", vf.createLiteral(atlassianHost.getOauthClientId()));
            }
            if (atlassianHost.getServiceEntitlementNumber() != null) {
                insert.setBinding("serviceEntitlementNumber", vf.createLiteral(atlassianHost.getServiceEntitlementNumber()));
            }

            insert.setBinding("activity", activity);
            insert.setBinding("timestamp", timestamp);
            insert.setBinding("value", value);
            insert.setBinding("addonInstalled", vf.createLiteral(atlassianHost.isAddonInstalled()));
            insert.setBinding("sharedSecret", vf.createLiteral(sharedSecretCyphertextBase64));
            insert.setBinding("keyIdentifier", vf.createLiteral(keyIdentifier));

            connection.begin();
            delete.execute();
            insert.execute();
            connection.commit();
        }

        hostCache.put(atlassianHost.getClientKey(), atlassianHost);
        hostCache.put(atlassianHost.getBaseUrl(), atlassianHost);

        return atlassianHost;
    }

    @Override
    public <S extends AtlassianHost> Iterable<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<AtlassianHost> findById(String id) {

        log.info("Calling findById for Id: {}", id);

        if (hostCache.containsKey(id)) {

            log.info("Host cache hit. Id: {}", id);
            return Optional.of(hostCache.get(id));
        }
        
        Repository hostRepository = hostRepositoryPool.getRepository(HOST_REPOSITORY_ID);

        try (RepositoryConnection connection = hostRepository.getConnection()) {

            connection.setIsolationLevel(IsolationLevels.NONE);

            TupleQuery query = SPARQLFactory.tupleQueryWithConnection(
                    connection, 
                    """
                    PREFIX prov: <http://www.w3.org/ns/prov#>
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    SELECT *
                    WHERE {
                        ?s a team:AtlassianHost ;
                            team:clientKey ?clientKey ;
                            team:baseUrl ?baseUrl ;
                            team:description ?description ;
                            team:productType ?productType ;
                            team:sharedSecret ?sharedSecret ;
                            team:keyIdentifier ?keyIdentifier ;
                            team:addonInstalled ?addonInstalled .
                        OPTIONAL { ?s team:oauthClientId ?oauthClientId }
                        OPTIONAL { ?s team:serviceEntitlementNumber ?serviceEntitlementNumber }
                    }
                    """);
            
            ValueFactory vf = hostRepository.getValueFactory();
            query.setBinding("clientKey", vf.createLiteral(id));

            try (TupleQueryResult result = query.evaluate()) {

                Optional<AtlassianHost> atlassianHost = result.stream().findFirst().map(bs -> {

                    String sharedSecretCyphertextBase64 = bs.getValue("sharedSecret").stringValue();
                    String keyIdentifier = bs.getValue("keyIdentifier").stringValue();

                    String sharedSecretPlaintext = sharedSecretEncryptorDecryptor.decrypt(sharedSecretCyphertextBase64, keyIdentifier);

                    AtlassianHost ah = new AtlassianHost();
                    ah.setBaseUrl(bs.getValue("baseUrl").stringValue());
                    ah.setClientKey(bs.getValue("clientKey").stringValue());
                    ah.setDescription(bs.getValue("description").stringValue());
                    ah.setProductType(bs.getValue("productType").stringValue());
                    ah.setSharedSecret(sharedSecretPlaintext);
                    ah.setAddonInstalled(Boolean.parseBoolean(bs.getValue("addonInstalled").stringValue()));

                    if (bs.hasBinding("oauthClientId")) {
                        ah.setOauthClientId(bs.getValue("oauthClientId").stringValue());
                    }
                    if (bs.hasBinding("serviceEntitlementNumber")) {
                        ah.setServiceEntitlementNumber(bs.getValue("serviceEntitlementNumber").stringValue());
                    }
                    
                    return ah;
                });

                if (atlassianHost.isPresent()) {
                    hostCache.put(atlassianHost.get().getClientKey(), atlassianHost.get());
                    hostCache.put(atlassianHost.get().getBaseUrl(), atlassianHost.get());
                }

                return atlassianHost;
            }
        }
    }

    @Override
    public boolean existsById(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<AtlassianHost> findAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<AtlassianHost> findAllById(Iterable<String> ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteById(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(AtlassianHost entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllById(Iterable<? extends String> ids) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll(Iterable<? extends AtlassianHost> entities) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<AtlassianHost> findFirstByBaseUrl(String baseUrl) {

        log.info("Calling findFirstByBaseUrl for URL: {}.", baseUrl);

        if (hostCache.containsKey(baseUrl)) {

            log.info("Host cache hit. BaseUrl: {}", baseUrl);
            return Optional.of(hostCache.get(baseUrl));
        }

        Repository hostRepository = hostRepositoryPool.getRepository(HOST_REPOSITORY_ID);

        try (RepositoryConnection connection = hostRepository.getConnection()) {

            connection.setIsolationLevel(IsolationLevels.NONE);

            TupleQuery query = SPARQLFactory.tupleQueryWithConnection(
                    connection, 
                    """
                    PREFIX prov: <http://www.w3.org/ns/prov#>
                    PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
                    SELECT *
                    WHERE {
                        ?s a team:AtlassianHost ;
                            team:clientKey ?clientKey ;
                            team:baseUrl ?baseUrl ;
                            team:description ?description ;
                            team:productType ?productType ;
                            team:sharedSecret ?sharedSecret ;
                            team:keyIdentifier ?keyIdentifier ;
                            team:addonInstalled ?addonInstalled .
                        OPTIONAL { ?s team:oauthClientId ?oauthClientId }
                        OPTIONAL { ?s team:serviceEntitlementNumber ?serviceEntitlementNumber }
                    }
                    """);
            
            ValueFactory vf = hostRepository.getValueFactory();
            query.setBinding("baseUrl", vf.createLiteral(baseUrl));

            try (TupleQueryResult result = query.evaluate()) {

                Optional<AtlassianHost> atlassianHost = result.stream().findFirst().map(bs -> {

                    String sharedSecretCyphertextBase64 = bs.getValue("sharedSecret").stringValue();
                    String keyIdentifier = bs.getValue("keyIdentifier").stringValue();

                    String sharedSecretPlaintext = sharedSecretEncryptorDecryptor.decrypt(sharedSecretCyphertextBase64, keyIdentifier);

                    AtlassianHost ah = new AtlassianHost();
                    ah.setBaseUrl(bs.getValue("baseUrl").stringValue());
                    ah.setClientKey(bs.getValue("clientKey").stringValue());
                    ah.setDescription(bs.getValue("description").stringValue());
                    ah.setProductType(bs.getValue("productType").stringValue());
                    ah.setSharedSecret(sharedSecretPlaintext);
                    ah.setAddonInstalled(Boolean.parseBoolean(bs.getValue("addonInstalled").stringValue()));

                    if (bs.hasBinding("oauthClientId")) {
                        ah.setOauthClientId(bs.getValue("oauthClientId").stringValue());
                    }
                    if (bs.hasBinding("serviceEntitlementNumber")) {
                        ah.setServiceEntitlementNumber(bs.getValue("serviceEntitlementNumber").stringValue());
                    }
                    
                    return ah;
                });

                if (atlassianHost.isPresent()) {
                    hostCache.put(atlassianHost.get().getClientKey(), atlassianHost.get());
                    hostCache.put(atlassianHost.get().getBaseUrl(), atlassianHost.get());
                }

                return atlassianHost;
            }
        }
    }

    @Override
    public Optional<AtlassianHost> findFirstByBaseUrlOrderByLastModifiedDateDesc(String baseUrl) {
        return findFirstByBaseUrl(baseUrl);
    }

    private String buildClientKeyIRIString(String clientKey) {
        return String.format("https://tfc.dalstonsemantics.com/client/%s", clientKey);
    }
}
