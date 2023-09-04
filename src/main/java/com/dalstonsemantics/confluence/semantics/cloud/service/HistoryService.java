package com.dalstonsemantics.confluence.semantics.cloud.service;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.connect.spring.AtlassianHost;
import com.dalstonsemantics.confluence.semantics.cloud.authentication.JwtBuilder;
import com.dalstonsemantics.confluence.semantics.cloud.domain.history.History;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to deal with Atlassian content requests related to content history.
 * The only way currently to get actual content of pages and blogs is to go via history API.
 */
@Component
@Slf4j
public class HistoryService {
    
    private JwtBuilder jwtBuilder;
    private RestTemplate restTemplate;

    public HistoryService(@Autowired JwtBuilder jwtBuilder, @Autowired RestTemplate restTemplate) {
        this.jwtBuilder = jwtBuilder;
        this.restTemplate = restTemplate;
    }
    
    @SneakyThrows
    public History getHistory(AtlassianHost host, String id, int version) {

        log.info("Geting History for Host: {}. Id: {}. Version: {}", host, id, version);

        String sharedSecret = host.getSharedSecret();
        String method = HttpMethod.GET.name();
        String apiPath = String.format("/rest/api/content/%s/version/%d", id, version);
        String contextPath = host.getBaseUrl();
        Map<String, String[]> parameterMap = new HashMap<>() {{ put("expand", new String[] { "content.body.storage" }); }};

        String requestUri = String.format("%s%s?expand=content.body.storage", contextPath, apiPath);

        String jwtToken = jwtBuilder.buildJwt(sharedSecret, method, apiPath, contextPath, parameterMap);

        HttpHeaders headers = new HttpHeaders() {{ set("Authorization", String.format("JWT %s", jwtToken)); }};
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        ResponseEntity<History> response = restTemplate.exchange(requestUri, HttpMethod.GET, request, History.class);
        History property = response.getBody();

        return property;
    }
}
