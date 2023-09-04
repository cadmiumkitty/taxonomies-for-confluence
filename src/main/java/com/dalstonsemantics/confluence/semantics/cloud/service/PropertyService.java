package com.dalstonsemantics.confluence.semantics.cloud.service;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.connect.spring.AtlassianHost;
import com.dalstonsemantics.confluence.semantics.cloud.authentication.JwtBuilder;
import com.dalstonsemantics.confluence.semantics.cloud.domain.property.Property;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to deal with Atlassian property requests (e.g. retrieving and updating propeties of pages and blogs).
 */
@Component
@Slf4j
public class PropertyService {
    
    private JwtBuilder jwtBuilder;
    private RestTemplate restTemplate;

    public PropertyService(@Autowired JwtBuilder jwtBuilder, @Autowired RestTemplate restTemplate) {
        this.jwtBuilder = jwtBuilder;
        this.restTemplate = restTemplate;
    }

    @SneakyThrows
    public Property getPropertyByContentIdByKey(AtlassianHost host, String contentId, String key) {

        log.info("Geting Property for Host: {}. Content Id: {}. Key: {}", host, contentId, key);

        return getPropertyByPathByKey(host, "/rest/api/content/%s".formatted(contentId), key);
    }

    @SneakyThrows
    public Property getPropertyByPathByKey(AtlassianHost host, String path, String key) {

        log.info("Geting Property for Host: {}. Path: {}. Key: {}", host, path);

        String sharedSecret = host.getSharedSecret();
        String method = HttpMethod.GET.name();
        String apiPath = "%s/property/%s".formatted(path, key);
        String contextPath = host.getBaseUrl();
        Map<String, String[]> parameterMap = new HashMap<>();

        String requestUri = "%s%s".formatted(contextPath, apiPath);

        String jwtToken = jwtBuilder.buildJwt(sharedSecret, method, apiPath, contextPath, parameterMap);

        HttpHeaders headers = new HttpHeaders() {{ 
            set(HttpHeaders.AUTHORIZATION, "JWT %s".formatted(jwtToken)); 
        }};
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<Property> response = restTemplate.exchange(requestUri, HttpMethod.GET, request, Property.class);
        Property property = response.getBody();

        return property;
    }

    @SneakyThrows
    public void updateProperty(AtlassianHost host, String contentId, String key, Property property) {

        log.info("Updating Property for Host: {}. Content Id: {}. Key: {}. Property: {}", host, contentId, key, property);

        String sharedSecret = host.getSharedSecret();
        String method = HttpMethod.PUT.name();
        String apiPath = "/rest/api/content/%s/property/%s".formatted(contentId, key);
        String contextPath = host.getBaseUrl();
        Map<String, String[]> parameterMap = new HashMap<>();

        String requestUri = "%s%s".formatted(contextPath, apiPath);

        String jwtToken = jwtBuilder.buildJwt(sharedSecret, method, apiPath, contextPath, parameterMap);

        HttpHeaders headers = new HttpHeaders() {{ 
            set(HttpHeaders.AUTHORIZATION, "JWT %s".formatted(jwtToken)); 
            set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE); 
        }};
        
        HttpEntity<Object> request = new HttpEntity<>(property, headers);
        
        restTemplate.exchange(requestUri, HttpMethod.PUT, request, Void.class);
    }

    @SneakyThrows
    public void deleteProperty(AtlassianHost host, String contentId, String key) {

        log.info("Deleting property for Host: {}. ContentId: {}. Key: {}", host, contentId, key);

        String sharedSecret = host.getSharedSecret();
        String method = HttpMethod.DELETE.name();
        String apiPath = "/rest/api/content/%s/property/%s".formatted(contentId, key);
        String contextPath = host.getBaseUrl();
        Map<String, String[]> parameterMap = new HashMap<>();

        String requestUri = "%s%s".formatted(contextPath, apiPath);

        String jwtToken = jwtBuilder.buildJwt(sharedSecret, method, apiPath, contextPath, parameterMap);

        HttpHeaders headers = new HttpHeaders() {{ 
            set(HttpHeaders.AUTHORIZATION, "JWT %s".formatted(jwtToken)); 
        }};
        
        HttpEntity<Object> request = new HttpEntity<>(headers);
        
        restTemplate.exchange(requestUri, HttpMethod.DELETE, request, Void.class);
    }    

}
