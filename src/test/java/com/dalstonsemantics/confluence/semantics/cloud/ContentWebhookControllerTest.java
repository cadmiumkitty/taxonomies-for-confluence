package com.dalstonsemantics.confluence.semantics.cloud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {TestAtlassianConnectContextArgumentResolverConfigurer.class, TestProviders.class, TestRepositories.class, TestServices.class})
@TestMethodOrder(OrderAnnotation.class)
public class ContentWebhookControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Order(10)
    public void shouldPostContentCreatedSubjectAndCreateEvent() throws IOException, URISyntaxException {

        String contentCreatedJson = Files.readString(Paths.get("./src/test/resources/requests/PageContentCreatedSubject.json"));
        RequestEntity<String> contentCreatedRequestEntity = RequestEntity
            .post(new URL(String.format("http://localhost:%d/webhook/content_created?lic=active", port)).toURI())
            .contentType(MediaType.APPLICATION_JSON)
            .body(contentCreatedJson); 
        ResponseEntity<String> contentCreatedResult = restTemplate.exchange(contentCreatedRequestEntity, String.class);

        assertEquals(200, contentCreatedResult.getStatusCode().value());
    }

    @Test
    @Order(20)
    public void shouldPostContentUpdatedSubjectAndCreateEvent() throws IOException, URISyntaxException {

        String postJson = Files.readString(Paths.get("./src/test/resources/requests/PageContentUpdatedSubject.json"));
        RequestEntity<String> contentCreatedRequestEntity = RequestEntity
            .post(new URL(String.format("http://localhost:%d/webhook/content_updated?lic=active", port)).toURI())
            .contentType(MediaType.APPLICATION_JSON)
            .body(postJson); 
        ResponseEntity<String> contentCreatedResult = restTemplate.exchange(contentCreatedRequestEntity, String.class);

        assertEquals(200, contentCreatedResult.getStatusCode().value());
    }

    @Test
    @Order(30)
    public void shouldPostContentRemovedSubjectAndCreateEvent() throws IOException, URISyntaxException {

        String contentCreatedJson = Files.readString(Paths.get("./src/test/resources/requests/PageContentRemovedSubject.json"));
        RequestEntity<String> contentCreatedRequestEntity = RequestEntity
            .post(new URL(String.format("http://localhost:%d/webhook/content_removed?lic=active", port)).toURI())
            .contentType(MediaType.APPLICATION_JSON)
            .body(contentCreatedJson); 
        ResponseEntity<String> contentCreatedResult = restTemplate.exchange(contentCreatedRequestEntity, String.class);

        assertEquals(200, contentCreatedResult.getStatusCode().value());
    }

    @Test
    @Order(40)
    public void shouldPostContentCreatedTypeAndCreateEvent() throws IOException, URISyntaxException {

        String contentCreatedJson = Files.readString(Paths.get("./src/test/resources/requests/PageContentCreatedType.json"));
        RequestEntity<String> contentCreatedRequestEntity = RequestEntity
            .post(new URL(String.format("http://localhost:%d/webhook/content_created?lic=active", port)).toURI())
            .contentType(MediaType.APPLICATION_JSON)
            .body(contentCreatedJson); 
        ResponseEntity<String> contentCreatedResult = restTemplate.exchange(contentCreatedRequestEntity, String.class);

        assertEquals(200, contentCreatedResult.getStatusCode().value());
    }

    @Test
    @Order(50)
    public void shouldPostContentUpdatedTypeAndCreateEvent() throws IOException, URISyntaxException {

        String postJson = Files.readString(Paths.get("./src/test/resources/requests/PageContentUpdatedType.json"));
        RequestEntity<String> contentCreatedRequestEntity = RequestEntity
            .post(new URL(String.format("http://localhost:%d/webhook/content_updated?lic=active", port)).toURI())
            .contentType(MediaType.APPLICATION_JSON)
            .body(postJson); 
        ResponseEntity<String> contentCreatedResult = restTemplate.exchange(contentCreatedRequestEntity, String.class);

        assertEquals(200, contentCreatedResult.getStatusCode().value());
    }

    @Test
    @Order(60)
    public void shouldPostContentRemovedTypeAndCreateEvent() throws IOException, URISyntaxException {

        String contentCreatedJson = Files.readString(Paths.get("./src/test/resources/requests/PageContentRemovedType.json"));
        RequestEntity<String> contentCreatedRequestEntity = RequestEntity
            .post(new URL(String.format("http://localhost:%d/webhook/content_removed?lic=active", port)).toURI())
            .contentType(MediaType.APPLICATION_JSON)
            .body(contentCreatedJson); 
        ResponseEntity<String> contentCreatedResult = restTemplate.exchange(contentCreatedRequestEntity, String.class);

        assertEquals(200, contentCreatedResult.getStatusCode().value());
    }    
}