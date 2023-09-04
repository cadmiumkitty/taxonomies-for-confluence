package com.dalstonsemantics.confluence.semantics.cloud.repository;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Simplest possible repository pool implemented as a Map.
 */
@Slf4j
public class Rdf4jRepositoryPool {

    private List<RemoteRepositoryManager> managers;
    private Model repositoryConfigTemplate;
    private Resource repositoryConfigNode;
    private Map<String, Repository> repositories;

    public Rdf4jRepositoryPool(List<RemoteRepositoryManager> managers, Model repositoryConfigTemplate, Resource repositoryConfigNode) {
        this.managers = managers;
        this.repositoryConfigTemplate = repositoryConfigTemplate;
        this.repositoryConfigNode = repositoryConfigNode;
        this.repositories = new ConcurrentHashMap<>();
    }

    public Repository getRepository(String repositoryId) {
        return repositories.computeIfAbsent(repositoryId, i -> {

            log.info("Repository does not exist. Creating for Id: {}. Using list: {}", i, managers);

            Model model = new DynamicModelFactory().createEmptyModel();
            model.addAll(repositoryConfigTemplate);
            model.add(this.repositoryConfigNode, iri("http://www.openrdf.org/config/repository#repositoryID"), literal(i));

            int repositoryIdHash = i.hashCode();
            int index = Math.abs(repositoryIdHash % managers.size());

            RemoteRepositoryManager manager = managers.get(index);
            manager.addRepositoryConfig(RepositoryConfig.create(model, this.repositoryConfigNode));

            return manager.getRepository(i);
        });
    }
}
