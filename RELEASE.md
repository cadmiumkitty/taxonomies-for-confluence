# Release history

## Release 1.3.0 on 14-Oct-2021

Initial release to include Relation Macro and About and Related Macro.

Accepting following risks with this release:
1. Macro rendering could not be fully performance-tested. Table of contents presents most risk.

## Release 1.4.0 on 18-Oct-2021

Added support for multiple formats and altLabel indexing to cover imports of the EU Publication Office controlled lists and support use cases from Atlassian Community Forum questions.

## Release 1.9.0 on 10-Nov-2021

Removes security vulnerabilities reported by OWASP10 Dependency Scanner and Azure Defender for Cloud.

## Release 1.10.0 on 21-Dec-2021

Adds taxonomy snapshots support. Adds user timezone support. Adds instructions for security testing implementation.

## Release 1.11.0 on 10-Jan-2022

Moves to new encryption key for the shared secret. Fixes caching for the relation macro, images and JS files.

## Release 1.12.0 on 01-Feb-2022

Upgrades Java and JavaScript dependencies to latest set of libraries compatible with Atlassian Connect, AtlasKit and Rdf4j.

## Release 1.13.0 on 02-Feb-2022

Improves query performance for retrieving concept schemes. Fixes timeout on QueueClient.receiveMessages to 60 seconds.

## Release 1.14.0 on 08-Feb-2022

Cleans up documentation. Fixes calculation of the impacted content (considers only "current" Statements of "current" content). Speeds up retrieval of the ConceptSchemes and related statistics (Concept and Statement count).

## Release 2.0.0 on 25-Feb-2022

Adds support for both dcterms:type and dcterms:subject. Adds simple taxonomy catalog. Adds SPARQL endpoint limited to content graph. Major functionality upgrade, not backwards-compatible, needs migration.

## Release 3.0.0 on 26-Feb-2022

Switches from skos:prefLabel to dcterms:label for pages and blog posts. Not backwards compatible, needs migration.

## Release 4.0.0 on 27-Feb-2022

Switches to correct content title property ot dcterms:label. Fixes provenance panel title.

## Release 4.1.0 on 11-Mar-2022

Switches to 3.7.5 of rdf4j, 9.0.59 of Tomcat.

## Release 4.2.0 on 22-Mar-2022

Fixes processing of SPARQL with POST requests.

## Release 4.3.0 on 13-Apr-2022

Bumps up file upload limit to 10M and rotates encryption key for sharedSeret.

## Release 4.4.0 on 19-Apr-2022

Removed DELETE scope. Moves to 3.7.7 of Rdf4j server. Moves away from Lucene Sail to simplify the set up, reduce the probability of index corruption due to NativeStore transacation failure and prepare for 4.0.0 migration when it is available.

## Release 4.5.0 on 23-Apr-2022

Moves to Rdf4j 3.0.0-M3 with fixed GH-3806 that resolves slow clean().

## Release 4.6.0 on 13-Jun-2022

Resolves concurrent update issue.

## Release 4.10.0 on 30-Oct-2022

Upgrades to Rdf4j 4.2.0. Implements CSP headers as per Atlassian Security Requirements.

## Release 4.11.0 on 10-Apr-2023

Upgrades to Rdf4j 4.2.3. Adds support for RDFS alongside SKOS. Adds SPARQL macro. Adds graph materialization to support more intuitive querying via SPARQL.

### Updated more details on the Marketplace

Taxonomies for Confluence add-on makes it faster to adapt Confluence to managing software development lifecycle, enterprise architecture, data governance, regulatory compliance, and risk management documentation. It enables:

Page classification in Confluence by type, subject and related concepts
Use of existing controlled vocabularies and reference data such as capability models, industry classifications, risk and control, or other corporate taxonomies for page classification
Tables of contents with type, subject and related concepts
Improved search by using preferred and alternative concept labels to search for Confluence content
Tracking of documentation coverage
Using SPARQL to query indexed Confluence content
Integration of Confluence into knowledge graphs using structured data and SPARQL federated queries.

### Updated release notes on the Marketplace

Adds support for RDFS Classes and Properties
Adds support for SPARQL macro on Confluence pages
Adds support for Wikibase-style use of tables

### Updated highlight 3

Annotate and query pages with RDFS and SPARQL. Import RDFS Classes and Properties, annotate pages using simple tables and query index pages with SPARQL. In this screenshot, you can see Confluence used as a data catalog.

## Release 4.12.0 on 07-May-2023

Upgrades to Rdf4j 4.2.4. Adds support for stable identifiers for resources.

## Release 4.13.0 on 15-May-2023

Enables SERVICE in SPARQL Macro. Simplifies copying of taxonomies and schemas between snapshots.

## Release 4.14.0 on 15-May-2023

Adds Class byline.

## Release 4.15.0 on 23-Jun-2023

Moves to version 2.0.0 of Data Governance Schema and Taxonomy. Attempts migration to 4.3.2 of RDF4j and 9.0.76 of Tomcat, rolls back changes (needs retest with next Tomcat).