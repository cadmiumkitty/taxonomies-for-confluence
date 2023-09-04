package com.dalstonsemantics.confluence.semantics.cloud;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.atlassian.connect.spring.AtlassianHost;
import com.dalstonsemantics.confluence.semantics.cloud.domain.user.User;
import com.dalstonsemantics.confluence.semantics.cloud.service.UserService;
import com.dalstonsemantics.confluence.semantics.cloud.vocabulary.TEAM;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProvenanceModelEnricher {
    
    private UserService userService;

    public ProvenanceModelEnricher(@Autowired UserService userService) {
        this.userService = userService;
    }

    public List<Statement> enrichProvenanceModel(AtlassianHost host, RepositoryConnection connection, Model model) {

        ValueFactory vf = connection.getValueFactory();

        Iterable<Statement> accountIds = model.getStatements(null, TEAM.ACCOUNT_ID, null);

        List<Statement> statements = StreamSupport.stream(accountIds.spliterator(), true)
            .flatMap(s -> {
                    User user = userService.getUserByAccountId(host, s.getObject().stringValue());
                    try {
                        URL baseUrl = new URL(host.getBaseUrl());
                        String imgUrl = "%s://%s%s".formatted(baseUrl.getProtocol(), baseUrl.getHost(), user.getProfilePicture().getPath());
                        Literal publicName = vf.createLiteral(user.getPublicName());
                        IRI profilePicture = vf.createIRI(imgUrl);
                        return Arrays.stream(
                            new Statement[]{
                                vf.createStatement(s.getSubject(), FOAF.NAME, publicName, s.getContext()),
                                vf.createStatement(s.getSubject(), FOAF.IMG, profilePicture, s.getContext())
                            });
                    } catch (MalformedURLException mue) {
                        throw new MalformedAtlassianHostBaseUrlException();
                    }
                })
            .collect(Collectors.toList());
        
        return statements;
    }
}
