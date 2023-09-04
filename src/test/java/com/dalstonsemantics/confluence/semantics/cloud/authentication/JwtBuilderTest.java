package com.dalstonsemantics.confluence.semantics.cloud.authentication;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import com.dalstonsemantics.confluence.semantics.cloud.provider.LocalDateTimeProvider;

public class JwtBuilderTest {
    
    @Test
    public void shouldCreateJwtToken() throws Exception {

        LocalDateTimeProvider mockLocalDateTimeProvider = Mockito.mock(LocalDateTimeProvider.class);
        Mockito.when(mockLocalDateTimeProvider.nowInUTC()).thenReturn(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));

        String key = "atlassian-connect-addon";
        long ttl = 180L;
        String sharedSecret = "XXXXX-XXXXX-XXXXX-XXXXX-XXXXX-XXXXX";
        String method = "GET";
        String baseUrl = "https://dalstonsemantics.atlassian.net/wiki";
        String contextPath = "/";
        String apiPath = "/rest/api/content/294914";
        Map<String, String[]> parameterMap = new HashMap<>();

        // See https://developer.atlassian.com/cloud/confluence/understanding-jwt/#creating-a-jwt-token
        String jwtToken = new JwtBuilder(mockLocalDateTimeProvider, key, ttl).buildJwt(
            sharedSecret, method, apiPath, contextPath, parameterMap);

        assertEquals("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJxc2giOiIzNTQ3ZWE4MGY4ODQ2ZGM4MTJmZTE4ZjJiMGVjMmYyMDUzYjM2NjA0MjViM2ZiYmQ4ZDlhZjQ4YThkYzUxOTlkIiwiaXNzIjoiYXRsYXNzaWFuLWNvbm5lY3QtYWRkb24iLCJleHAiOjE4MCwiaWF0IjowfQ.rVXTBFhMAvapyWeuNH_00DvBDdNlBuKWEfIjuKH5hbg", jwtToken);
    }
}
