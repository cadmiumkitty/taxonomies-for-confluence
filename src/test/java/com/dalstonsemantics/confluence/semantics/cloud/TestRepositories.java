package com.dalstonsemantics.confluence.semantics.cloud;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.lucene.LuceneSail;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;

import org.mockito.Mockito;

import com.dalstonsemantics.confluence.semantics.cloud.repository.Rdf4jRepositoryPool;

@TestConfiguration
public class TestRepositories {
    
    @Bean(name = "HostRepositoryPool")
    @Primary
    public Rdf4jRepositoryPool getHostRepositoryPool(@Value("${addon.repositories.host.data-dir}") String dataDir) throws IOException {

        NativeStore ns = new NativeStore(new File(dataDir));
        SailRepository sr = new SailRepository(ns);
        sr.getConnection().clear();
        sr.getConnection().add(Paths.get("./src/test/resources/repositories/host/default.ttl").toFile(), RDFFormat.TURTLE);

        Rdf4jRepositoryPool pool = Mockito.mock(Rdf4jRepositoryPool.class);
        Mockito.when(pool.getRepository(Mockito.any())).thenReturn(sr);

        return pool;
    }

    @Bean(name = "TaxonomyRepositoryPool")
    @Primary
    public Rdf4jRepositoryPool getTaxonomyRepositoryPool(@Value("${addon.repositories.taxonomy.data-dir}") String dataDir,
    @Value("${addon.repositories.taxonomy.index-dir}") String indexDir) throws Exception {
        NativeStore ns = new NativeStore(new File(dataDir));

        String indexfields = String.join("\n", 
            "index.1=http://www.w3.org/2004/02/skos/core#prefLabel",
            "index.2=http://www.w3.org/2004/02/skos/core#altLabel",
            "index.3=http://www.w3.org/2004/02/skos/core#notation");

        LuceneSail luceneSail = new LuceneSail();
        luceneSail.setParameter(LuceneSail.LUCENE_DIR_KEY, indexDir);
        luceneSail.setParameter(LuceneSail.INDEXEDFIELDS, indexfields);
        luceneSail.setBaseSail(ns);

        SailRepository sr = new SailRepository(luceneSail);
        sr.getConnection().clear();

        Rdf4jRepositoryPool pool = Mockito.mock(Rdf4jRepositoryPool.class);
        Mockito.when(pool.getRepository(Mockito.any())).thenReturn(sr);

        return pool;
    }    
}
