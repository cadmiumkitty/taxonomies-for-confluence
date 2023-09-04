# Operations cheat sheet

## Listing all clients

```
PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>

SELECT * 
WHERE {
  ?s a team:AtlassianHost ;
    team:baseUrl ?o .
}
```

## Listing all concept schemes

```
PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>

SELECT * 
WHERE {
  ?cs a skos:ConceptScheme .
}
```

## Manually getting rid of the statements and related content

This may happen in case the content was removed and trashed but the call back from Confluence failed. Graph name is the next version of the taxonomy.

```
PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX prov: <http://www.w3.org/ns/prov#>

SELECT ?s ?subject ?predicate ?object ?status ?activity ?value
WHERE {
  <https://tfc.dalstonsemantics.com/taxonomy/927294f7-0a9f-3d01-8120-b3ca3a45df38-31644469-a64f-4ea6-a4f6-56f13e0e9c62> team:impactedContent ?subject .
  ?s a rdf:Statement ;
    rdf:subject ?subject ;
    rdf:predicate ?predicate ;
    rdf:object ?object ;
    team:status ?status .
  OPTIONAL {?activity prov:generated ?s . 
    OPTIONAL {?activity prov:used ?value . }
  }
}
```

## Finding orphaned pages and statements

Statements of all pages marked as `trashed`.

```
PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX prov: <http://www.w3.org/ns/prov#>

SELECT *
WHERE {
  GRAPH <https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38> {
    ?s a rdf:Statement ;
      rdf:subject ?subject ;
      rdf:predicate ?predicate ;
      rdf:object ?object ;
      team:status ?status .
    ?subject team:status "trashed" .
  }
}
```

Statements of all pages marked as `removed`.

```
PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX prov: <http://www.w3.org/ns/prov#>

SELECT *
WHERE {
  GRAPH <https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38> {
    ?s a rdf:Statement ;
      rdf:subject ?subject ;
      rdf:predicate ?predicate ;
      rdf:object ?object ;
      team:status ?status .
    ?subject team:status "removed" .
  }
}
```

All pages and their status.

```
PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX prov: <http://www.w3.org/ns/prov#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>

SELECT *
WHERE {
  GRAPH <https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38> {
    ?s a team:page ;
      dcterms:title ?title ;
      team:status ?status .
  }
}
```

Pages that are in `removed` status.

```
PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX prov: <http://www.w3.org/ns/prov#>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>

SELECT *
WHERE {
  GRAPH <https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38> {
    ?s a team:page ;
      dcterms:title ?title ;
      team:status "removed" .
  }
}
```

## Clean up

```
PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX prov: <http://www.w3.org/ns/prov#>

SELECT ?statement ?activity ?value ?subject
WHERE {
  GRAPH <https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38> {
    ?statement a rdf:Statement ;
      rdf:subject ?subject ;
      rdf:predicate ?predicate ;
      rdf:object ?object ;
      team:status ?status .
    ?subject team:status "removed" .
    ?activity a prov:Activity ;
      prov:generated ?statement ;
      prov:used ?value .
  }
}
```

## Migration from skos:prefLabel to dcterms:title for content

PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX dcterms: <http://purl.org/dc/terms/>

WITH <https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38> 
DELETE { 
	?s skos:prefLabel ?label .
} 
INSERT { 
	?s dcterms:title ?label .
} 
WHERE { 
	?s a team:page ;
		skos:prefLabel ?label . 
}

## Fix bug with incorrect property

PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX dcterms: <http://purl.org/dc/terms/>

WITH <https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38> 
DELETE { 
	?s dcterms:label ?label .
} 
INSERT { 
	?s dcterms:title ?label .
} 
WHERE { 
	?s a team:page ;
		dcterms:label ?label . 
}

## Delete all pages that are in "removed" state

```
PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX prov: <http://www.w3.org/ns/prov#>

WITH <https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38> 
DELETE { 
	?page ?pageP ?pageO .
} 
INSERT { 
} 
WHERE { 
  ?page a team:page ;
		team:status "removed" .
	?page ?pageP ?pageO .
}
```

## Delete all blog posts that are in "removed" state

```
PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX prov: <http://www.w3.org/ns/prov#>

WITH <https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38> 
DELETE { 
	?page ?pageP ?pageO .
} 
INSERT { 
} 
WHERE { 
  ?page a team:blogpost ;
		team:status "removed" .
	?page ?pageP ?pageO .
}
```

## Delete all statements that are for "removed" pages

```
PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX prov: <http://www.w3.org/ns/prov#>

WITH <https://tfc.dalstonsemantics.com/content/927294f7-0a9f-3d01-8120-b3ca3a45df38> 
DELETE { 
	?statement ?statementP ?statementO .
	?activity ?activityP ?activityO .
	?value ?valueP ?valueO .
} 
INSERT { 
} 
WHERE { 
    ?statement a rdf:Statement ;
      rdf:subject ?subject ;
      rdf:predicate ?predicate ;
      rdf:object ?object ;
      team:status ?status .
    ?subject team:status "removed" .
    ?activity a prov:Activity ;
      prov:generated ?statement ;
      prov:used ?value .
	?statement ?statementP ?statementO .
	?activity ?activityP ?activityO .
	?value ?valueP ?valueO .
}
```

## Clean up of the workshop installation (emorozov@zohomail.com) for videos

```
PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
PREFIX prov: <http://www.w3.org/ns/prov#>
INSERT {
	GRAPH <https://tfc.dalstonsemantics.com/taxonomy-version/b50d1755-a4a4-3053-81d1-f0e1d6659a58> {
		<https://tfc.dalstonsemantics.com/taxonomy/b50d1755-a4a4-3053-81d1-f0e1d6659a58> a team:TaxonomyGraph ;
			team:status "current" ;
			team:conceptSchemeCount 0 ;
			team:conceptCount 0 ;
			team:insertedConceptCount 0 ;
			team:updatedConceptCount 0 ;
			team:deletedConceptCount 0 ;
			team:impactedContentCount 0 ;
			team:processedContentCount 0 ;
			team:failedContentCount 0 .
		<https://tfc.dalstonsemantics.com/taxonomy/b50d1755-a4a4-3053-81d1-f0e1d6659a58-da25db75-eb30-4f0b-824d-04bcd7b6d777> a team:TaxonomyGraph ;
			team:previousTaxonomyGraph <https://tfc.dalstonsemantics.com/taxonomy/b50d1755-a4a4-3053-81d1-f0e1d6659a58> ;
			team:status "draft" ;
			team:conceptSchemeCount 0 ;
			team:conceptCount 0 ;
			team:insertedConceptCount 0 ;
			team:updatedConceptCount 0 ;
			team:deletedConceptCount 0 .
		<https://tfc.dalstonsemantics.com/agent/60f87c67411fae0070962c87> a prov:Agent ;
			team:accountId "60f87c67411fae0070962c87" .
		<https://tfc.dalstonsemantics.com/activity/ca80b4f1-4c2c-47c0-a5f4-3473f40b02e3> a prov:Activity ;
			prov:startedAtTime "2022-07-10T00:45:07.690266805Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
			prov:endedAtTime "2022-07-10T00:45:07.690266805Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
			prov:wasAssociatedWith <https://tfc.dalstonsemantics.com/agent/60f87c67411fae0070962c87> ;
			prov:generated <https://tfc.dalstonsemantics.com/taxonomy/b50d1755-a4a4-3053-81d1-f0e1d6659a58> ;
			prov:generated <https://tfc.dalstonsemantics.com/taxonomy/b50d1755-a4a4-3053-81d1-f0e1d6659a58-da25db75-eb30-4f0b-824d-04bcd7b6d777> .
	}
}
WHERE {
}
```

## Add new properties for old Taxonomy Versions Release 4.11.0

PREFIX team: <https://dalstonsemantics.com/ns/com/atlassian/>
INSERT {
  GRAPH ?taxonomyVersionGraph {
    ?taxonomyGraph
      team:classCount 0 ;
      team:propertyCount 0 ;
      team:insertedClassCount 0 ;
      team:updatedClassCount 0 ;
      team:deletedClassCount 0 ;
      team:insertedPropertyCount 0 ;
      team:updatedPropertyCount 0 ;
      team:deletedPropertyCount 0 .
  }
}
WHERE {
  GRAPH ?taxonomyVersionGraph {
    ?taxonomyGraph a team:TaxonomyGraph .
  }
}