package com.dalstonsemantics.confluence.semantics.cloud.repository;

import static org.eclipse.rdf4j.model.util.Values.iri;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.rdf4j.http.client.util.HttpClientBuilders;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Host Repository is common and is used to store details of all Atlassian Hosts.
 * Taxonomy repository is Atlassian Host specific.
 */
@Configuration
public class Rdf4JRepositoryConfig {

    @Bean(name = "HostRepositoryPool")
    public Rdf4jRepositoryPool getHostRepository(@Value("classpath:repositories/host.ttl") Resource repositoryConfigResource,
            @Value("#{'${addon.repositories.host.server}'.split(',')}") List<String> servers,
            @Value("${addon.repositories.host.username}") String username,
            @Value("${addon.repositories.host.password}") String password,
            @Value("${addon.http.client.max-conn-per-route}") int maxConnPerRoute,
            @Value("${addon.http.client.max-conn-total}") int maxConnTotal,
            @Value("${addon.http.client.connection-time-to-live-min}") int connectionTimeToLiveMin,
            @Value("${addon.http.connection-manager.validate-after-inactivity-millis}") int validateAfterInactivityMillis) throws IOException {

        IRI repositoryConfigNode = iri("https://tfc.dalstonsemantics.com/repository/host");
        return creatRdf4jRepositoryPool(repositoryConfigResource, repositoryConfigNode, servers, username, password, 
            maxConnPerRoute, maxConnTotal, connectionTimeToLiveMin, validateAfterInactivityMillis);
    }

    @Bean(name = "TaxonomyRepositoryPool")
    public Rdf4jRepositoryPool getTaxonomyRepository(
            @Value("classpath:repositories/taxonomy.ttl") Resource repositoryConfigResource,
            @Value("#{'${addon.repositories.host.server}'.split(',')}") List<String> servers,
            @Value("${addon.repositories.taxonomy.username}") String username,
            @Value("${addon.repositories.taxonomy.password}") String password,
            @Value("${addon.http.client.max-conn-per-route}") int maxConnPerRoute,
            @Value("${addon.http.client.max-conn-total}") int maxConnTotal,
            @Value("${addon.http.client.connection-time-to-live-min}") int connectionTimeToLiveMin,
            @Value("${addon.http.connection-manager.validate-after-inactivity-millis}") int validateAfterInactivityMillis ) throws IOException {

        IRI repositoryConfigNode = iri("https://tfc.dalstonsemantics.com/repository/taxonomy");
        return creatRdf4jRepositoryPool(repositoryConfigResource, repositoryConfigNode, servers, username, password, 
            maxConnPerRoute, maxConnTotal, connectionTimeToLiveMin, validateAfterInactivityMillis);
    }

    private Rdf4jRepositoryPool creatRdf4jRepositoryPool(Resource repositoryConfigResource, IRI repositoryConfigNode, 
            List<String> servers, String username, String password,           
            int maxConnPerRoute, int maxConnTotal, int connectionTimeToLiveMin, int validateAfterInactivityMillis) throws IOException {

        try (InputStream is = repositoryConfigResource.getInputStream()) {
            Model repositoryConfig = Rio.parse(is, RDFFormat.TURTLE);
            List<RemoteRepositoryManager> repositoryManagers = servers.stream().map(s -> {

                PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
                connectionManager.setValidateAfterInactivity(validateAfterInactivityMillis);

                HttpClientBuilder builder = HttpClientBuilders.getSSLTrustAllHttpClientBuilder();
                builder.setConnectionManager(connectionManager);
                builder.setMaxConnPerRoute(maxConnPerRoute);
                builder.setMaxConnTotal(maxConnTotal);
                builder.setConnectionTimeToLive(connectionTimeToLiveMin, TimeUnit.MINUTES);
                
                RemoteRepositoryManager manager = RemoteRepositoryManager.getInstance(s, username, password);
                manager.setHttpClient(builder.build());

                return manager;
            }).collect(Collectors.toList());
            return new Rdf4jRepositoryPool(repositoryManagers, repositoryConfig, repositoryConfigNode);            
        }
    }
}