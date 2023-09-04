package com.dalstonsemantics.confluence.semantics.cloud.vocabulary;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

public class TEAM {
    
	public static final String NAMESPACE = "https://dalstonsemantics.com/ns/com/atlassian/";
	public static final String PREFIX = "team";

    public static final IRI ATLASSIAN_HOST = iri(NAMESPACE, "AtlassianHost");

    public static final IRI CLIENT_KEY = iri(NAMESPACE, "clientKey");

    public static final IRI ACCOUNT_ID = iri(NAMESPACE, "accountId");

    public static final IRI CONTENT = iri(NAMESPACE, "content");
    public static final IRI CONTENT_ID = iri(NAMESPACE, "contentId");
    public static final IRI CONTENT_TYPE = iri(NAMESPACE, "contentType");
    public static final IRI CONTENT_VERSION = iri(NAMESPACE, "contentVersion");
    public static final IRI CONTENT_SPACE_KEY = iri(NAMESPACE, "contentSpaceKey");

    public static final IRI PROPERTY_ID = iri(NAMESPACE, "propertyId");
    public static final IRI PROPERTY_VERSION = iri(NAMESPACE, "propertyVersion");

    public static final IRI MACRO_ID = iri(NAMESPACE, "macroId");

    public static final IRI TABLE_ID = iri(NAMESPACE, "tableId");

    public static final IRI STATUS = iri(NAMESPACE, "status");

    public static final String CURRENT_STRING = "current";
    public static final String TRASHED_STRING = "trashed";
    public static final String REMOVED_STRING = "removed";
    public static final String DRAFT_STRING = "draft";
    public static final String HISTORICAL_STRING = "historical";
    public static final String IMPORTING_STRING = "importing";
    public static final String COPYING_STRING = "copying";
    public static final String CLEARING_STRING = "clearing";
    public static final String CALCULATING_TAXONOMY_VERSION_DIFFERENCE_STRING = "calculating_taxonomy_version_difference";
    public static final String CALCULATING_CONTENT_IMPACT_STRING = "calculating_content_impact";
    public static final String AWAITING_TRANSITION_TO_CURRENT_STRING = "awaiting_transition_to_current";
    public static final String TRANSITIONING_TO_CURRENT_STRING = "transitioning_to_current";
    public static final String CANCELLING_TRANSITION_TO_CURRENT_STRING = "cancelling_transition_to_current";

    public static final Literal CURRENT = literal(CURRENT_STRING);
    public static final Literal TRASHED = literal(TRASHED_STRING);
    public static final Literal REMOVED = literal(REMOVED_STRING);
    public static final Literal DRAFT = literal(DRAFT_STRING);
    public static final Literal HISTORICAL = literal(HISTORICAL_STRING);
    public static final Literal IMPORTING = literal(IMPORTING_STRING);
    public static final Literal COPYING = literal(COPYING_STRING);
    public static final Literal CLEARING = literal(CLEARING_STRING);
    public static final Literal CALCULATING_TAXONOMY_VERSION_DIFFERENCE = literal(CALCULATING_TAXONOMY_VERSION_DIFFERENCE_STRING);
    public static final Literal CALCULATING_CONTENT_IMPACT = literal(CALCULATING_CONTENT_IMPACT_STRING);
    public static final Literal AWAITING_TRANSITION_TO_CURRENT = literal(AWAITING_TRANSITION_TO_CURRENT_STRING);
    public static final Literal TRANSITIONING_TO_CURRENT = literal(TRANSITIONING_TO_CURRENT_STRING);
    public static final Literal CANCELLING_TRANSITION_TO_CURRENT = literal(CANCELLING_TRANSITION_TO_CURRENT_STRING);

    public static final IRI TAXONOMY_GRAPH = iri(NAMESPACE, "TaxonomyGraph");
    public static final IRI PREVIOUS_TAXONOMY_GRAPH = iri(NAMESPACE, "previousTaxonomyGraph");
    public static final IRI STATUS_TRANSITION_EVENT = iri(NAMESPACE, "statusTransitionEvent");
    public static final IRI CONCEPT_SCHEME_COUNT = iri(NAMESPACE, "conceptSchemeCount");
    public static final IRI CONCEPT_COUNT = iri(NAMESPACE, "conceptCount");
    public static final IRI INSERTED_CONCEPT = iri(NAMESPACE, "insertedConcept");
    public static final IRI UPDATED_CONCEPT = iri(NAMESPACE, "updatedConcept");
    public static final IRI DELETED_CONCEPT = iri(NAMESPACE, "deletedConcept");
    public static final IRI INSERTED_CONCEPT_COUNT = iri(NAMESPACE, "insertedConceptCount");
    public static final IRI UPDATED_CONCEPT_COUNT = iri(NAMESPACE, "updatedConceptCount");
    public static final IRI DELETED_CONCEPT_COUNT = iri(NAMESPACE, "deletedConceptCount");
    public static final IRI IMPACTED_CONTENT = iri(NAMESPACE, "impactedContent");
    public static final IRI IMPACTED_CONTENT_COUNT = iri(NAMESPACE, "impactedContentCount");
    public static final IRI PROCESSED_CONTENT = iri(NAMESPACE, "processedContent");
    public static final IRI PROCESSED_CONTENT_COUNT = iri(NAMESPACE, "processedContentCount");
    public static final IRI FAILED_CONTENT = iri(NAMESPACE, "failedContent");
    public static final IRI FAILED_CONTENT_COUNT = iri(NAMESPACE, "failedContentCount");

    public static final IRI TARGET_TAXONOMY_GRAPH = iri(NAMESPACE, "targetTaxonomyGraph");

    public static final IRI IMPORT_FILE_EVENT = iri(NAMESPACE, "ImportFileEvent");
    public static final IRI BLOB_NAME = iri(NAMESPACE, "blobName");

    public static final IRI IMPORT_RESOURCE_EVENT = iri(NAMESPACE, "ImportResourceEvent");
    public static final IRI RESOURCE = iri(NAMESPACE, "resource");

    public static final IRI COPY_FROM_CURRENT_EVENT = iri(NAMESPACE, "CopyFromCurrentEvent");

    public static final IRI CLEAR_EVENT = iri(NAMESPACE, "ClearEvent");

    public static final IRI CALCULATE_TAXONOMY_VERSION_DIFFERENCE_EVENT = iri(NAMESPACE, "CalculateTaxonomyVersionDifferenceEvent");

    public static final IRI CALCULATE_CONTENT_IMPACT_EVENT = iri(NAMESPACE, "CalculateContentImpactEvent");

    public static final IRI TRANSITION_TO_CURRENT_EVENT = iri(NAMESPACE, "TransitionToCurrentEvent");

    public static final IRI CANCEL_TRANSITION_TO_CURRENT_EVENT = iri(NAMESPACE, "CancelTransitionToCurrentEvent");

    public static final IRI CALLBACK_CONTENT_CREATED_REMOVED_RESTORED_TRASHED_UPDATED_EVENT = iri(NAMESPACE, "CallbackContentCreatedRemovedRestoredTrashedUpdatedEvent");

    public static final IRI CALLBACK_PROPERTY_CREATED_UPDATED_EVENT = iri(NAMESPACE, "CallbackPropertyCreatedUpdatedEvent");

    public static final IRI CALLBACK_PROPERTY_REMOVED_EVENT = iri(NAMESPACE, "CallbackPropertyRemovedEvent");

    public static final IRI EVENT_CONTENT_ID = iri(NAMESPACE, "eventContentId");
    public static final IRI EVENT_CONTENT_SPACE_KEY = iri(NAMESPACE, "eventContentSpaceKey");
    public static final IRI EVENT_CONTENT_TITLE = iri(NAMESPACE, "eventContentTitle");
    public static final IRI EVENT_CONTENT_TYPE = iri(NAMESPACE, "eventContentType");
    public static final IRI EVENT_CONTENT_SELF = iri(NAMESPACE, "eventContentSelf");
    public static final IRI EVENT_CONTENT_CONTAINER = iri(NAMESPACE, "eventContentContainer");
    public static final IRI EVENT_CONTENT_VERSION = iri(NAMESPACE, "eventContentVersion");
    public static final IRI EVENT_CONTENT_WHEN = iri(NAMESPACE, "eventContentWhen");

    public static final IRI MATERIALIZE_CONTENT_GRAPH_EVENT = iri(NAMESPACE, "MaterializeContentGraphEvent");
}
