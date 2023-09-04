package com.dalstonsemantics.confluence.semantics.cloud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.mockito.Mockito;

import com.dalstonsemantics.confluence.semantics.cloud.domain.history.Body;
import com.dalstonsemantics.confluence.semantics.cloud.domain.history.History;
import com.dalstonsemantics.confluence.semantics.cloud.domain.history.Storage;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Icon;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Name;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Property;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Tooltip;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Value;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;
import com.dalstonsemantics.confluence.semantics.cloud.service.ContentService;
import com.dalstonsemantics.confluence.semantics.cloud.service.HistoryService;
import com.dalstonsemantics.confluence.semantics.cloud.service.PropertyService;

public class TestServicesUtils {
    
    public static final void resetUUIDProviderSequence(UUIDProvider uuidProvider) {

        List<UUID> uuids = IntStream.range(1, 2000).boxed().map(i -> UUID.fromString(String.format("b7279f95-1820-4582-8088-f7d065f%05d", i))).collect(Collectors.toList());
        Mockito.when(uuidProvider.randomUUID()).thenReturn(
            UUID.fromString("b7279f95-1820-4582-8088-f7d065fd116d"),
            uuids.toArray(new UUID[0]));
    }

    public static final void resetPageContentToVersionOne(ContentService contentService, HistoryService historyService) throws IOException {

        com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content contentServicePageContent = com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content.builder()
            .id("294914")
            .type("page")
            .spaceKey("FI")
            .title("Retail Banking Support Updated")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Version.builder()
                .number(1)
                .build())
            .links(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Links.builder()
                .base("https://dalstonsemantics.atlassian.net/wiki")
                .webui("/spaces/VM/pages/294914/Retail+Banking+Support+Updated")
                .build())
            .build();
        
        Mockito.when(contentService.getContentById(Mockito.any(), Mockito.eq("294914"))).thenReturn(contentServicePageContent);
        Mockito.when(contentService.getContentByPath(Mockito.any(), Mockito.eq("/rest/api/content/294914"))).thenReturn(contentServicePageContent);

        Calendar when = Calendar.getInstance();
        when.set(2021, 0, 1, 10, 20, 30);
        when.set(Calendar.MILLISECOND, 40);
        when.set(Calendar.ZONE_OFFSET, 60000 * 5);

        com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content historyServicePageContent = com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content.builder()
            .id("294914")
            .type("page")
            .title("Retail Banking Support Updated")
            .body(Body.builder()
                .storage(Storage.builder()
                    .value(Files.readString(Paths.get("./src/test/resources/responses/history/page-294914.xhtml")))
                    .build())
                .build())
            .build();
        
        History pageHistory = History.builder()
            .content(historyServicePageContent)
            .when(when)
            .build();

        Mockito.when(historyService.getHistory(Mockito.any(), Mockito.eq("294914"), Mockito.eq(1))).thenReturn(pageHistory);
    }

    public static final void resetPageContentToVersionTwoWithUpdatedContent(ContentService contentService, HistoryService historyService) throws IOException  {
        
        com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content contentServicePageContent = com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content.builder()
            .id("294914")
            .type("page")
            .spaceKey("FI")
            .title("Retail Banking Support Updated")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Version.builder()
                .number(2)
                .build())
            .links(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Links.builder()
                .base("https://dalstonsemantics.atlassian.net/wiki")
                .webui("/spaces/VM/pages/294914/Retail+Banking+Support+Updated")
                .build())
            .build();
        
        Mockito.when(contentService.getContentById(Mockito.any(), Mockito.eq("294914"))).thenReturn(contentServicePageContent);
        Mockito.when(contentService.getContentByPath(Mockito.any(), Mockito.eq("/rest/api/content/294914"))).thenReturn(contentServicePageContent);

        Calendar when = Calendar.getInstance();
        when.set(2023, 0, 17, 20, 10, 05);
        when.set(Calendar.MILLISECOND, 40);
        when.set(Calendar.ZONE_OFFSET, 60000 * 5);

        com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content historyServicePageContent = com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content.builder()
            .id("294914")
            .type("page")
            .title("Retail Banking Support Updated")
            .body(Body.builder()
                .storage(Storage.builder()
                    .value(Files.readString(Paths.get("./src/test/resources/responses/history/page-294914-updated.xhtml")))
                    .build())
                .build())
            .build();
        
        History pageHistory = History.builder()
            .content(historyServicePageContent)
            .when(when)
            .build();

        Mockito.when(historyService.getHistory(Mockito.any(), Mockito.eq("294914"), Mockito.eq(2))).thenReturn(pageHistory);
    }

    public static final void resetBlogContentToVersionOne(ContentService contentService, HistoryService historyService) throws IOException  {

        com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content contentServiceBlogContent = com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content.builder()
            .id("1192525828")
            .type("blogpost")
            .spaceKey("~323528440")
            .title("Simple Blog Post")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Version.builder()
                .number(1)
                .build())
            .links(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Links.builder()
                .base("https://dalstonsemantics.atlassian.net/wiki")
                .webui("/spaces/~323528440/blog/2021/09/19/1192525828/Simple+Blog+Post")
                .build())
            .build();

        Mockito.when(contentService.getContentById(Mockito.any(), Mockito.eq("1192525828"))).thenReturn(contentServiceBlogContent);
        Mockito.when(contentService.getContentByPath(Mockito.any(), Mockito.eq("/rest/api/content/1192525828"))).thenReturn(contentServiceBlogContent);

        Calendar when = Calendar.getInstance();
        when.set(2021, 0, 1, 10, 20, 30);
        when.set(Calendar.MILLISECOND, 40);
        when.set(Calendar.ZONE_OFFSET, 60000 * 5);

        com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content historyServiceBlogContent = com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content.builder()
            .id("1192525828")
            .type("blogpost")
            .title("Title")
            .body(Body.builder()
                .storage(Storage.builder()
                    .value(Files.readString(Paths.get("./src/test/resources/responses/history/blog-1192525828.xhtml")))
                    .build())
                .build())
            .build();
        
        History blogHistory = History.builder()
            .content(historyServiceBlogContent)
            .when(when)
            .build();

        Mockito.when(historyService.getHistory(Mockito.any(), Mockito.eq("1192525828"), Mockito.eq(2))).thenReturn(blogHistory);        
    }

    public static final void resetBlogContentToVersionTwoWithUpdatedContent(ContentService contentService, HistoryService historyService) throws IOException  {
        
        com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content contentServiceBlogContent = com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content.builder()
            .id("1192525828")
            .type("blogpost")
            .spaceKey("~323528440")
            .title("Simple Blog Post")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Version.builder()
                .number(2)
                .build())
            .links(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Links.builder()
                .base("https://dalstonsemantics.atlassian.net/wiki")
                .webui("/spaces/~323528440/blog/2021/09/19/1192525828/Simple+Blog+Post")
                .build())
            .build();

        Mockito.when(contentService.getContentById(Mockito.any(), Mockito.eq("1192525828"))).thenReturn(contentServiceBlogContent);
        Mockito.when(contentService.getContentByPath(Mockito.any(), Mockito.eq("/rest/api/content/1192525828"))).thenReturn(contentServiceBlogContent);

        Calendar when = Calendar.getInstance();
        when.set(2023, 0, 17, 20, 10, 05);
        when.set(Calendar.MILLISECOND, 40);
        when.set(Calendar.ZONE_OFFSET, 60000 * 5);

        com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content historyServiceBlogContent = com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content.builder()
            .id("1192525828")
            .type("blogpost")
            .title("Title")
            .body(Body.builder()
                .storage(Storage.builder()
                    .value(Files.readString(Paths.get("./src/test/resources/responses/history/blog-1192525828-updated.xhtml")))
                    .build())
                .build())
            .build();
        
        History blogHistory = History.builder()
            .content(historyServiceBlogContent)
            .when(when)
            .build();

        Mockito.when(historyService.getHistory(Mockito.any(), Mockito.eq("1192525828"), Mockito.eq(2))).thenReturn(blogHistory);
    }

    public static final void resetPagePropertyToVersionOne(PropertyService propertyService) throws IOException  {

        Property pagePropertyA = Property.builder()
            .id("1191804929")
            .key("taxonomies-for-confluence-subject")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.property.Version.builder()
                .number(1)
                .build())
            .value(Value.builder()
                .uri("https://www.abs.gov.au/ausstats/anzsic/A")
                .name(Name.builder()
                    .value("Agriculture, Forestry and Fishing")
                    .build())
                .tooltip(Tooltip.builder()
                    .value("Agriculture, Forestry and Fishing")
                    .build())
                .icon(Icon.builder()
                    .width(16)
                    .height(16)
                    .url("icons/byline-subject-01.png")
                    .build())
                .build())
            .build();

        Mockito.when(propertyService.getPropertyByPathByKey(Mockito.any(), Mockito.eq("/rest/api/content/294914"), Mockito.eq("taxonomies-for-confluence-subject"))).thenReturn(pagePropertyA);
    }

    public static final void resetPagePropertyToVersionTwoWithUpdatedExtractions(PropertyService propertyService) throws IOException  {

        Property pagePropertyB = Property.builder()
            .id("1191804929")
            .key("taxonomies-for-confluence-subject")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.property.Version.builder()
                .number(2)
                .build())
            .value(Value.builder()
                .uri("https://www.abs.gov.au/ausstats/anzsic/B")
                .name(Name.builder()
                    .value("Mining")
                    .build())
                .tooltip(Tooltip.builder()
                    .value("Mining")
                    .build())
                .icon(Icon.builder()
                    .width(16)
                    .height(16)
                    .url("icons/byline-subject-01.png")
                    .build())
                .build())
            .build();

        Mockito.when(propertyService.getPropertyByPathByKey(Mockito.any(), Mockito.eq("/rest/api/content/294914"), Mockito.eq("taxonomies-for-confluence-subject"))).thenReturn(pagePropertyB);
    }
}
