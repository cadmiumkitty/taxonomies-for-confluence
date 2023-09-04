package com.dalstonsemantics.confluence.semantics.cloud.util;

import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.repository.RepositoryConnection;

/**
 * Just a way to clean up preparation of SPARQL queries (i.e. setting inference to false and formatting the query 
 * where we can't do somthing in SPARQL natively such as restricting results to a particular version of the graph).
 */
public class SPARQLFactory {

    public static Update updateWithConnection(RepositoryConnection connection, String template, Object... replacements) {
        Update update = connection.prepareUpdate(QueryLanguage.SPARQL, template.formatted(replacements));
        update.setIncludeInferred(false);
        return update;
    }

    public static TupleQuery tupleQueryWithConnection(RepositoryConnection connection, String template, Object... replacements) {
        TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, template.formatted(replacements));
        tupleQuery.setIncludeInferred(false);
        return tupleQuery;
    }

    public static GraphQuery graphQueryWithConnection(RepositoryConnection connection, String template, Object... replacements) {
        GraphQuery graphQuery = connection.prepareGraphQuery(QueryLanguage.SPARQL, template.formatted(replacements));
        graphQuery.setIncludeInferred(false);
        return graphQuery;
    }
}
