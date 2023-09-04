package com.dalstonsemantics.confluence.semantics.cloud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.dalstonsemantics.confluence.semantics.cloud.domain.addon.AddOn;
import com.dalstonsemantics.confluence.semantics.cloud.domain.addon.License;
import com.dalstonsemantics.confluence.semantics.cloud.domain.content.Contents;
import com.dalstonsemantics.confluence.semantics.cloud.domain.history.Body;
import com.dalstonsemantics.confluence.semantics.cloud.domain.history.History;
import com.dalstonsemantics.confluence.semantics.cloud.domain.history.Storage;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Icon;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Name;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Property;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Tooltip;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Value;
import com.dalstonsemantics.confluence.semantics.cloud.domain.user.Operation;
import com.dalstonsemantics.confluence.semantics.cloud.domain.user.ProfilePicture;
import com.dalstonsemantics.confluence.semantics.cloud.domain.user.User;
import com.dalstonsemantics.confluence.semantics.cloud.service.AddOnService;
import com.dalstonsemantics.confluence.semantics.cloud.service.ContentService;
import com.dalstonsemantics.confluence.semantics.cloud.service.HistoryService;
import com.dalstonsemantics.confluence.semantics.cloud.service.PropertyService;
import com.dalstonsemantics.confluence.semantics.cloud.service.UserService;

@TestConfiguration
public class TestServices {

    @Bean
    @Primary
    public UserService createMockUserService() {

        User user = User.builder()
            .accountId("557058:d092c978-5ab7-4c8a-8d87-32ffac22c584")
            .accountType("atlassian")
            .publicName("Eugene Morozov")
            .operation(Operation.builder()
                .operation("administer")
                .targetType("application")
                .build())
            .profilePicture(ProfilePicture.builder()
                .path("/wiki/aa-avatar/557058:d092c978-5ab7-4c8a-8d87-32ffac22c584")
                .width(48)
                .height(48)
                .isDefault(false)
                .build())
            .build();

        UserService mockUserService = Mockito.mock(UserService.class);
        Mockito.when(mockUserService.getUserByAccountId(Mockito.any(), Mockito.any())).thenReturn(user);
        Mockito.when(mockUserService.getCurrentUser(Mockito.any())).thenReturn(user);
        return mockUserService;
    }

    @Bean
    @Primary
    public ContentService createMockContentService() {

        com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content pageContent = com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content.builder()
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

        com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content blogContent = com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content.builder()
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

        com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content globalContent = com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content.builder()
            .id("1142095879")
            .type("global")
            .title("FI")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Version.builder()
                .number(1)
                .build())
            .links(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Links.builder()
                .base("https://dalstonsemantics.atlassian.net/wiki")
                .webui("/spaces/FI")
                .build())
            .build();

        com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content sampleSparqlPageFi = com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content.builder()
            .id("10010")
            .type("page")
            .spaceKey("FI")
            .title("Sample SPARQL Page")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Version.builder()
                .number(1)
                .build())
            .links(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Links.builder()
                .base("https://dalstonsemantics.atlassian.net/wiki")
                .webui("/spaces/FI/pages/10010/Sample+SPARQL+Page")
                .build())
            .build();

        com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content sampleSparqlPageVm = com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content.builder()
            .id("10020")
            .type("page")
            .spaceKey("VM")
            .title("Sample SPARQL Page")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Version.builder()
                .number(1)
                .build())
            .links(com.dalstonsemantics.confluence.semantics.cloud.domain.content.Links.builder()
                .base("https://dalstonsemantics.atlassian.net/wiki")
                .webui("/spaces/VM/pages/10020/Sample+SPARQL+Page")
                .build())
            .build();

        Contents sampleSparqlPageFiContents = Contents.builder()
            .results(Arrays.asList(sampleSparqlPageFi))
            .start(0)
            .limit(25)
            .size(1)
            .build();

        Contents sampleSparqlPageVmContents = Contents.builder()
            .results(Arrays.asList(sampleSparqlPageVm))
            .start(0)
            .limit(25)
            .size(1)
            .build();

        Contents pageContentContents = Contents.builder()
            .results(Arrays.asList(pageContent))
            .start(0)
            .limit(25)
            .size(1)
            .build();

        ContentService mockContentService = Mockito.mock(ContentService.class);
        Mockito.when(mockContentService.getContentById(Mockito.any(), Mockito.eq("294914"))).thenReturn(pageContent);
        Mockito.when(mockContentService.getContentByPath(Mockito.any(), Mockito.eq("/rest/api/content/294914"))).thenReturn(pageContent);
        Mockito.when(mockContentService.getContentById(Mockito.any(), Mockito.eq("1192525828"))).thenReturn(blogContent);
        Mockito.when(mockContentService.getContentByPath(Mockito.any(), Mockito.eq("/rest/api/content/1192525828"))).thenReturn(blogContent);
        Mockito.when(mockContentService.getContentById(Mockito.any(), Mockito.eq("1142095879"))).thenReturn(globalContent);
        Mockito.when(mockContentService.getContentByPath(Mockito.any(), Mockito.eq("/rest/api/space/FI"))).thenReturn(globalContent);
        Mockito.when(mockContentService.getContentById(Mockito.any(), Mockito.eq("10010"))).thenReturn(sampleSparqlPageFi);
        Mockito.when(mockContentService.getContentByPath(Mockito.any(), Mockito.eq("/rest/api/content/10010"))).thenReturn(sampleSparqlPageFi);
        Mockito.when(mockContentService.getContents(Mockito.any(), Mockito.eq("page"), Mockito.eq("FI"), Mockito.eq("Sample SPARQL Page"))).thenReturn(sampleSparqlPageFiContents);
        Mockito.when(mockContentService.getContents(Mockito.any(), Mockito.eq("page"), Mockito.eq("VM"), Mockito.eq("Sample SPARQL Page"))).thenReturn(sampleSparqlPageVmContents);
        Mockito.when(mockContentService.getContents(Mockito.any(), Mockito.eq("page"), Mockito.eq("FI"), Mockito.eq("Retail Banking Support Updated"))).thenReturn(pageContentContents);
        return mockContentService;
    }

    @Bean
    @Primary
    public AddOnService createAddOnService() {

        AddOn addOn = AddOn.builder()
            .key("taxonomies-for-confluence")
            .version("1.2.0")
            .state("ENABLED")
            .license(License.builder()
                .active(true)
                .type("COMMERCIAL")
                .evaluation(false)
                .supportEntitlementNumber("SEN-123")
                .build())
            .build();

        AddOnService mockAddOnService = Mockito.mock(AddOnService.class);
        Mockito.when(mockAddOnService.getAddOn(Mockito.any())).thenReturn(addOn);
        return mockAddOnService;
    }

    @Bean
    @Primary
    public PropertyService createPropertyService() {

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

        Property pagePropertyB = Property.builder()
            .id("2191804929")
            .key("taxonomies-for-confluence-type")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.property.Version.builder()
                .number(1)
                .build())
            .value(Value.builder()
                .uri("https://dalstonsemantics.com/ns/org/isbn-international/978-0124158290/0002")
                .name(Name.builder()
                    .value("Policy")
                    .build())
                .tooltip(Tooltip.builder()
                    .value("Policy")
                    .build())
                .icon(Icon.builder()
                    .width(16)
                    .height(16)
                    .url("icons/byline-type-01.png")
                    .build())
                .build())
            .build();

        Property blogPropertyA = Property.builder()
            .id("1192591361")
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

        Property blogPropertyBSubject = Property.builder()
            .id("101")
            .key("taxonomies-for-confluence-subject")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.property.Version.builder()
                .number(1)
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

        Property blogPropertyCSubject = Property.builder()
            .id("102")
            .key("taxonomies-for-confluence-subject")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.property.Version.builder()
                .number(1)
                .build())
            .value(Value.builder()
            .uri("https://www.abs.gov.au/ausstats/anzsic/C")
            .name(Name.builder()
                .value("Manufacturing")
                .build())
            .tooltip(Tooltip.builder()
                .value("Manufacturing")
                .build())
            .icon(Icon.builder()
                .width(16)
                .height(16)
                .url("icons/byline-subject-01.png")
                .build())
            .build())
        .build();

        Property blogPropertyDSubject = Property.builder()
            .id("103")
            .key("taxonomies-for-confluence-subject")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.property.Version.builder()
                .number(1)
                .build())
            .value(Value.builder()
            .uri("https://www.abs.gov.au/ausstats/anzsic/D")
            .name(Name.builder()
                .value("Electricity, Gas, Water and Waste Services")
                .build())
            .tooltip(Tooltip.builder()
                .value("Electricity, Gas, Water and Waste Services")
                .build())
            .icon(Icon.builder()
                .width(16)
                .height(16)
                .url("icons/byline-subject-01.png")
                .build())
            .build())
        .build();

        Property blogPropertyBType = Property.builder()
            .id("111")
            .key("taxonomies-for-confluence-type")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.property.Version.builder()
                .number(1)
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
                .url("icons/byline-type-01.png")
                .build())
            .build())
        .build();

        Property blogPropertyCType = Property.builder()
            .id("112")
            .key("taxonomies-for-confluence-type")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.property.Version.builder()
                .number(1)
                .build())
            .value(Value.builder()
            .uri("https://www.abs.gov.au/ausstats/anzsic/C")
            .name(Name.builder()
                .value("Manufacturing")
                .build())
            .tooltip(Tooltip.builder()
                .value("Manufacturing")
                .build())
            .icon(Icon.builder()
                .width(16)
                .height(16)
                .url("icons/byline-type-01.png")
                .build())
            .build())
        .build();

        Property blogPropertyDType = Property.builder()
            .id("113")
            .key("taxonomies-for-confluence-type")
            .version(com.dalstonsemantics.confluence.semantics.cloud.domain.property.Version.builder()
                .number(1)
                .build())
            .value(Value.builder()
            .uri("https://www.abs.gov.au/ausstats/anzsic/D")
            .name(Name.builder()
                .value("Electricity, Gas, Water and Waste Services")
                .build())
            .tooltip(Tooltip.builder()
                .value("Electricity, Gas, Water and Waste Services")
                .build())
            .icon(Icon.builder()
                .width(16)
                .height(16)
                .url("icons/byline-type-01.png")
                .build())
            .build())
        .build();
        
        PropertyService mockPropertyService = Mockito.mock(PropertyService.class);
        Mockito.when(mockPropertyService.getPropertyByPathByKey(Mockito.any(), Mockito.eq("/rest/api/content/294914"), Mockito.eq("taxonomies-for-confluence-subject"))).thenReturn(pagePropertyA);
        Mockito.when(mockPropertyService.getPropertyByPathByKey(Mockito.any(), Mockito.eq("/rest/api/content/294914"), Mockito.eq("taxonomies-for-confluence-type"))).thenReturn(pagePropertyB);
        Mockito.when(mockPropertyService.getPropertyByPathByKey(Mockito.any(), Mockito.eq("/rest/api/content/1192525828"), Mockito.eq("taxonomies-for-confluence-subject"))).thenReturn(blogPropertyA);
        Mockito.when(mockPropertyService.getPropertyByPathByKey(Mockito.any(), Mockito.eq("/rest/api/content/201"), Mockito.eq("taxonomies-for-confluence-subject"))).thenReturn(blogPropertyBSubject);
        Mockito.when(mockPropertyService.getPropertyByPathByKey(Mockito.any(), Mockito.eq("/rest/api/content/202"), Mockito.eq("taxonomies-for-confluence-subject"))).thenReturn(blogPropertyCSubject);
        Mockito.when(mockPropertyService.getPropertyByPathByKey(Mockito.any(), Mockito.eq("/rest/api/content/203"), Mockito.eq("taxonomies-for-confluence-subject"))).thenReturn(blogPropertyDSubject);
        Mockito.when(mockPropertyService.getPropertyByPathByKey(Mockito.any(), Mockito.eq("/rest/api/content/201"), Mockito.eq("taxonomies-for-confluence-type"))).thenReturn(blogPropertyBType);
        Mockito.when(mockPropertyService.getPropertyByPathByKey(Mockito.any(), Mockito.eq("/rest/api/content/202"), Mockito.eq("taxonomies-for-confluence-type"))).thenReturn(blogPropertyCType);
        Mockito.when(mockPropertyService.getPropertyByPathByKey(Mockito.any(), Mockito.eq("/rest/api/content/203"), Mockito.eq("taxonomies-for-confluence-type"))).thenReturn(blogPropertyDType);
        Mockito.when(mockPropertyService.getPropertyByContentIdByKey(Mockito.any(), Mockito.eq("294914"), Mockito.eq("taxonomies-for-confluence-subject"))).thenReturn(pagePropertyA);
        Mockito.when(mockPropertyService.getPropertyByContentIdByKey(Mockito.any(), Mockito.eq("294914"), Mockito.eq("taxonomies-for-confluence-type"))).thenReturn(pagePropertyB);
        Mockito.when(mockPropertyService.getPropertyByContentIdByKey(Mockito.any(), Mockito.eq("1192525828"), Mockito.eq("taxonomies-for-confluence-subject"))).thenReturn(blogPropertyA);
        Mockito.when(mockPropertyService.getPropertyByContentIdByKey(Mockito.any(), Mockito.eq("201"), Mockito.eq("taxonomies-for-confluence-subject"))).thenReturn(blogPropertyBSubject);
        Mockito.when(mockPropertyService.getPropertyByContentIdByKey(Mockito.any(), Mockito.eq("202"), Mockito.eq("taxonomies-for-confluence-subject"))).thenReturn(blogPropertyCSubject);
        Mockito.when(mockPropertyService.getPropertyByContentIdByKey(Mockito.any(), Mockito.eq("203"), Mockito.eq("taxonomies-for-confluence-subject"))).thenReturn(blogPropertyDSubject);
        Mockito.when(mockPropertyService.getPropertyByContentIdByKey(Mockito.any(), Mockito.eq("201"), Mockito.eq("taxonomies-for-confluence-type"))).thenReturn(blogPropertyBType);
        Mockito.when(mockPropertyService.getPropertyByContentIdByKey(Mockito.any(), Mockito.eq("202"), Mockito.eq("taxonomies-for-confluence-type"))).thenReturn(blogPropertyCType);
        Mockito.when(mockPropertyService.getPropertyByContentIdByKey(Mockito.any(), Mockito.eq("203"), Mockito.eq("taxonomies-for-confluence-type"))).thenReturn(blogPropertyDType);
        
        return mockPropertyService;
    }

    @Bean
    @Primary
    public HistoryService createHistoryService() throws ParseException, IOException {

        Calendar when = Calendar.getInstance();
        when.set(2021, 0, 1, 10, 20, 30);
        when.set(Calendar.MILLISECOND, 40);
        when.set(Calendar.ZONE_OFFSET, 60000 * 5);

        com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content pageContent = com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content.builder()
            .id("294914")
            .type("page")
            .title("Retail Banking Support Updated")
            .body(Body.builder()
                .storage(Storage.builder()
                    .value(Files.readString(Paths.get("./src/test/resources/responses/history/page-294914.xhtml")))
                    .build())
                .build())
            .build();

        com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content blogContent = com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content.builder()
            .id("1192525828")
            .type("blogpost")
            .title("Title")
            .body(Body.builder()
                .storage(Storage.builder()
                    .value(Files.readString(Paths.get("./src/test/resources/responses/history/blog-1192525828.xhtml")))
                    .build())
                .build())
            .build();

        com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content pageContent201 = com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content.builder()
            .id("201")
            .type("page")
            .title("Title")
            .body(Body.builder()
                .storage(Storage.builder()
                    .value(Files.readString(Paths.get("./src/test/resources/responses/history/page-20X.xhtml")))
                    .build())
                .build())
            .build();
        
        com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content pageContent202 = com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content.builder()
            .id("202")
            .type("page")
            .title("Title")
            .body(Body.builder()
                .storage(Storage.builder()
                    .value(Files.readString(Paths.get("./src/test/resources/responses/history/page-20X.xhtml")))
                    .build())
                .build())
            .build();

        com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content pageContent203 = com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content.builder()
            .id("203")
            .type("page")
            .title("Title")
            .body(Body.builder()
                .storage(Storage.builder()
                    .value(Files.readString(Paths.get("./src/test/resources/responses/history/page-20X.xhtml")))
                    .build())
                .build())
            .build();

        com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content pageContent204 = com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content.builder()
            .id("204")
            .type("page")
            .title("Title")
            .body(Body.builder()
                .storage(Storage.builder()
                    .value(Files.readString(Paths.get("./src/test/resources/responses/history/page-20X.xhtml")))
                    .build())
                .build())
            .build();

        com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content pageContent301 = com.dalstonsemantics.confluence.semantics.cloud.domain.history.Content.builder()
            .id("301")
            .type("page")
            .title("Title")
            .body(Body.builder()
                .storage(Storage.builder()
                    .value(Files.readString(Paths.get("./src/test/resources/responses/history/page-30X.xhtml")))
                    .build())
                .build())
            .build();

        History pageHistory = History.builder()
            .content(pageContent)
            .when(when)
            .build();

        History blogHistory = History.builder()
            .content(blogContent)
            .when(when)
            .build();

        History pageHistory201 = History.builder()
            .content(pageContent201)
            .when(when)
            .build();

        History pageHistory202 = History.builder()
            .content(pageContent202)
            .when(when)
            .build();

        History pageHistory203 = History.builder()
            .content(pageContent203)
            .when(when)
            .build();

        History pageHistory204 = History.builder()
            .content(pageContent204)
            .when(when)
            .build();
        
        History pageHistory301 = History.builder()
            .content(pageContent301)
            .when(when)
            .build();

        HistoryService mockHistoryService = Mockito.mock(HistoryService.class);
        Mockito.when(mockHistoryService.getHistory(Mockito.any(), Mockito.eq("294914"), Mockito.anyInt())).thenReturn(pageHistory);
        Mockito.when(mockHistoryService.getHistory(Mockito.any(), Mockito.eq("1192525828"), Mockito.anyInt())).thenReturn(blogHistory);
        Mockito.when(mockHistoryService.getHistory(Mockito.any(), Mockito.eq("201"), Mockito.anyInt())).thenReturn(pageHistory201);
        Mockito.when(mockHistoryService.getHistory(Mockito.any(), Mockito.eq("202"), Mockito.anyInt())).thenReturn(pageHistory202);
        Mockito.when(mockHistoryService.getHistory(Mockito.any(), Mockito.eq("203"), Mockito.anyInt())).thenReturn(pageHistory203);
        Mockito.when(mockHistoryService.getHistory(Mockito.any(), Mockito.eq("204"), Mockito.anyInt())).thenReturn(pageHistory204);
        Mockito.when(mockHistoryService.getHistory(Mockito.any(), Mockito.eq("301"), Mockito.anyInt())).thenReturn(pageHistory301);

        return mockHistoryService;
    }
}
