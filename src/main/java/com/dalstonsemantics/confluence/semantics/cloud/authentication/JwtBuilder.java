package com.dalstonsemantics.confluence.semantics.cloud.authentication;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.util.Map;

import com.atlassian.jwt.SigningAlgorithm;
import com.atlassian.jwt.core.writer.JsonSmartJwtJsonBuilder;
import com.atlassian.jwt.core.writer.JwtClaimsBuilder;
import com.atlassian.jwt.core.writer.NimbusJwtWriterFactory;
import com.atlassian.jwt.httpclient.CanonicalHttpUriRequest;
import com.atlassian.jwt.writer.JwtJsonBuilder;
import com.atlassian.jwt.writer.JwtWriterFactory;
import com.dalstonsemantics.confluence.semantics.cloud.provider.LocalDateTimeProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtBuilder {
    
    private LocalDateTimeProvider ldtp;
    private String key;
    private long ttl;

    public JwtBuilder(@Autowired LocalDateTimeProvider ldtp, @Value("${addon.key}") String key,  @Value("${addon.jwt.ttl}") long ttl) {
        this.ldtp = ldtp;
        this.key = key;
        this.ttl = ttl;
    }

    public String buildJwt(String sharedSecret, String method, String apiPath, String contextPath, Map<String, String[]> parameterMap) 
                    throws UnsupportedEncodingException, NoSuchAlgorithmException {

        long issuedAt = ldtp.nowInUTC().toEpochSecond(ZoneOffset.UTC);
        long expiresAt = issuedAt + this.ttl;

        JwtJsonBuilder jwtBuilder = new JsonSmartJwtJsonBuilder()
                .issuedAt(issuedAt)
                .expirationTime(expiresAt)
                .issuer(key);

        CanonicalHttpUriRequest canonical = new CanonicalHttpUriRequest(method, apiPath, contextPath, parameterMap);
        JwtClaimsBuilder.appendHttpRequestClaims(jwtBuilder, canonical);

        JwtWriterFactory jwtWriterFactory = new NimbusJwtWriterFactory();
        String jwtbuilt = jwtBuilder.build();
        String jwtToken = jwtWriterFactory.macSigningWriter(SigningAlgorithm.HS256,
                sharedSecret).jsonToJwt(jwtbuilt);

        return jwtToken;
    }
}
