package com.dalstonsemantics.confluence.semantics.cloud.service;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.connect.spring.AtlassianHost;
import com.dalstonsemantics.confluence.semantics.cloud.authentication.JwtBuilder;
import com.dalstonsemantics.confluence.semantics.cloud.cache.CacheConfig;
import com.dalstonsemantics.confluence.semantics.cloud.domain.user.User;

import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to deal with Atlassian users (e.g. retrieving user details to surface them via UI or validating user permissions for things like taxonomy management).
 */
@Component
@Slf4j
public class UserService {
    
    private JwtBuilder jwtBuilder;
    private RestTemplate restTemplate;
    private Cache<String, User> userCache;

    public UserService(@Autowired JwtBuilder jwtBuilder, @Autowired RestTemplate restTemplate, @Autowired @Qualifier(CacheConfig.USER_CACHE_NAME) Cache<String, User> userCache) {
        this.jwtBuilder = jwtBuilder;
        this.restTemplate = restTemplate;
        this.userCache = userCache;
    }
    
    @SneakyThrows
    public User getUserByAccountId(AtlassianHost host, String accountId) {
        
        log.info("Geting user. Account id: {}", accountId);

        String sharedSecret = host.getSharedSecret();
        String method = HttpMethod.GET.name();
        String apiPath = "/rest/api/user";
        String contextPath = host.getBaseUrl();
        Map<String, String[]> parameterMap = new HashMap<>() {{
            put("accountId", new String[] { accountId });
            put("expand", new String[] { "operations" });
        }};

        String requestUri = "%s%s?accountId=%s&expand=operations".formatted(contextPath, apiPath, accountId);

        if (userCache.containsKey(requestUri)) {
            log.info("User cache hit. Key: {}", requestUri);
            return userCache.get(requestUri);
        }

        String jwtToken = jwtBuilder.buildJwt(sharedSecret, method, apiPath, contextPath, parameterMap);

        HttpHeaders headers = new HttpHeaders() {{ set("Authorization", "JWT %s".formatted(jwtToken)); }};
        
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<User> response = restTemplate.exchange(requestUri, HttpMethod.GET, request, User.class);
        User user = response.getBody();

        userCache.put(requestUri, user);

        return user;
    }

    @SneakyThrows
    public User getCurrentUser(AtlassianHost host) {
        
        log.info("Geting current user");

        String sharedSecret = host.getSharedSecret();
        String method = HttpMethod.GET.name();
        String apiPath = "/rest/api/user/current";
        String contextPath = host.getBaseUrl();
        Map<String, String[]> parameterMap = new HashMap<>();

        String requestUri = "%s%s".formatted(contextPath, apiPath);

        if (userCache.containsKey(requestUri)) {
            log.info("User cache hit. Key: {}", requestUri);
            return userCache.get(requestUri);
        }

        String jwtToken = jwtBuilder.buildJwt(sharedSecret, method, apiPath, contextPath, parameterMap);

        HttpHeaders headers = new HttpHeaders() {{ set("Authorization", "JWT %s".formatted(jwtToken)); }};
        
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<User> response = restTemplate.exchange(requestUri, HttpMethod.GET, request, User.class);
        User user = response.getBody();

        userCache.put(requestUri, user);

        return user;
    }    
}
