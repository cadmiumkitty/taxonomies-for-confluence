package com.dalstonsemantics.confluence.semantics.cloud.provider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;

import org.springframework.stereotype.Component;

@Component
public class CryptographyClientProvider {
    
    private DefaultAzureCredential credential;
    private ConcurrentMap<String, CryptographyClient> cryptographyClients;

    public CryptographyClientProvider() {
        this.credential = new DefaultAzureCredentialBuilder().build();
        this.cryptographyClients = new ConcurrentHashMap<>();
    }

    public CryptographyClient getCryptographyClient(String keyIdentifier) {
        return cryptographyClients.computeIfAbsent(keyIdentifier, x -> {
            return new CryptographyClientBuilder()
                .keyIdentifier(x)
                .credential(credential)
                .buildClient();
        });
    }
}
