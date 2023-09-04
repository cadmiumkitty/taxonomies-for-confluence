package com.dalstonsemantics.confluence.semantics.cloud.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.atlassian.connect.spring.AtlassianHost;
import com.dalstonsemantics.confluence.semantics.cloud.authentication.JwtBuilder;
import com.dalstonsemantics.confluence.semantics.cloud.domain.content.Content;
import com.dalstonsemantics.confluence.semantics.cloud.domain.content.Contents;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to deal with Atlassian content requests (e.g. retrieving and updating pages).
 */
@Component
@Slf4j
public class ContentService {
    
    private JwtBuilder jwtBuilder;
    private RestTemplate restTemplate;

    public ContentService(@Autowired JwtBuilder jwtBuilder, @Autowired RestTemplate restTemplate) {
        this.jwtBuilder = jwtBuilder;
        this.restTemplate = restTemplate;
    }
    
    @SneakyThrows
    public Contents getContents(AtlassianHost host, String type, String spaceKey, String title) {
        
        log.info("Geting content. Type: {}. Space key: {}. Title: {}", type, spaceKey, title);

        String path = "/rest/api/content";

        log.info("Geting Content for Host: {}. Path: {}", host, path);

        String sharedSecret = host.getSharedSecret();
        String method = HttpMethod.GET.name();
        String contextPath = host.getBaseUrl();

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("type", new String[] { type });
        parameterMap.put("spaceKey", new String[] { spaceKey });
        parameterMap.put("title", new String[] { title });
        parameterMap.put("expand", new String[] { "version" });

        String requestUri = "%s%s?type=%s&spaceKey=%s&title=%s&expand=version".formatted(contextPath, path, 
                URLEncoder.encode(type, StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(spaceKey, StandardCharsets.UTF_8.toString()),
                URLEncoder.encode(title, StandardCharsets.UTF_8.toString()));

        String jwtToken = jwtBuilder.buildJwt(sharedSecret, method, path, contextPath, parameterMap);

        HttpHeaders headers = new HttpHeaders() {{ set("Authorization", "JWT %s".formatted(jwtToken)); }};
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<Contents> response = restTemplate.exchange(requestUri, HttpMethod.GET, request, Contents.class);
        Contents contents = response.getBody();

        log.info("Response: {}", response);

        return contents;
    }

    @SneakyThrows
    public Content getContentById(AtlassianHost host, String id) {
        
        log.info("Geting content. Id: {}", id);

        String path = "/rest/api/content/%s".formatted(id);

        return getContent(host, path);
    }

    @SneakyThrows
    public Content getContentByPath(AtlassianHost host, String path) {
        
        log.info("Geting content. API path: {}", path);

        return getContent(host, path);
    }

    @SneakyThrows
    private Content getContent(AtlassianHost host, String path) {

        log.info("Geting Content for Host: {}. Path: {}", host, path);

        String sharedSecret = host.getSharedSecret();
        String method = HttpMethod.GET.name();
        String contextPath = host.getBaseUrl();
        Map<String, String[]> parameterMap = new HashMap<>();

        String requestUri = "%s%s".formatted(contextPath, path);

        String jwtToken = jwtBuilder.buildJwt(sharedSecret, method, path, contextPath, parameterMap);

        HttpHeaders headers = new HttpHeaders() {{ set("Authorization", "JWT %s".formatted(jwtToken)); }};
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<Content> response = restTemplate.exchange(requestUri, HttpMethod.GET, request, Content.class);
        Content content = response.getBody();

        return content;
    }

    @SneakyThrows
    public void updateContent(AtlassianHost host, String id, Content content) {

        log.info("Updating Content for Host: {}. Id: {}", host, id);

        String sharedSecret = host.getSharedSecret();
        String method = HttpMethod.PUT.name();
        String path = "/rest/api/content/%s".formatted(id);
        String contextPath = host.getBaseUrl();
        Map<String, String[]> parameterMap = new HashMap<>();

        String requestUri = "%s%s".formatted(contextPath, path);

        String jwtToken = jwtBuilder.buildJwt(sharedSecret, method, path, contextPath, parameterMap);

        HttpHeaders headers = new HttpHeaders() {{ 
            set(HttpHeaders.AUTHORIZATION, "JWT %s".formatted(jwtToken)); 
            set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE); 
            set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        }};
        
        HttpEntity<Content> request = new HttpEntity<>(content, headers);
        
        restTemplate.exchange(requestUri, HttpMethod.PUT, request, Void.class);
    }

}
