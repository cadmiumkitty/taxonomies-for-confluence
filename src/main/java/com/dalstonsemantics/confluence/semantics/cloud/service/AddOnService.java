package com.dalstonsemantics.confluence.semantics.cloud.service;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.connect.spring.AtlassianHost;
import com.dalstonsemantics.confluence.semantics.cloud.authentication.JwtBuilder;
import com.dalstonsemantics.confluence.semantics.cloud.cache.CacheConfig;
import com.dalstonsemantics.confluence.semantics.cloud.domain.addon.AddOn;

import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to retrieve add-on information (e.g. licences).
 */
@Component
@Slf4j
public class AddOnService {

    private JwtBuilder jwtBuilder;
    private RestTemplate restTemplate;
    private Cache<String, AddOn> addOnCache;
    private String addOnKey;

    public AddOnService(@Autowired JwtBuilder jwtBuilder, @Autowired RestTemplate restTemplate, 
            @Autowired @Qualifier(CacheConfig.ADDON_CACHE_NAME) Cache<String, AddOn> addOnCache,
            @Value("${addon.key}") String addOnKey) {
        this.jwtBuilder = jwtBuilder;
        this.restTemplate = restTemplate;
        this.addOnCache = addOnCache;
        this.addOnKey = addOnKey;
    }
    
    @SneakyThrows
    public AddOn getAddOn(AtlassianHost host) {
        
        log.info("Geting AddOn Info for Host: {}", host);

        String sharedSecret = host.getSharedSecret();
        String method = HttpMethod.GET.name();
        String apiPath = String.format("/rest/atlassian-connect/1/addons/%s", addOnKey);
        String contextPath = host.getBaseUrl();
        Map<String, String[]> parameterMap = new HashMap<>();

        String requestUri = String.format("%s%s", contextPath, apiPath);

        if (addOnCache.containsKey(requestUri)) {
            log.info("AddOn cache hit. Key: {}", requestUri);
            return addOnCache.get(requestUri);
        }

        String jwtToken = jwtBuilder.buildJwt(sharedSecret, method, apiPath, contextPath, parameterMap);

        HttpHeaders headers = new HttpHeaders() {{ set("Authorization", String.format("JWT %s", jwtToken)); }};
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<AddOn> response = restTemplate.exchange(requestUri, HttpMethod.GET, request, AddOn.class);
        AddOn addOn = response.getBody();

        addOnCache.put(requestUri, addOn);

        return addOn;
    }    
}
