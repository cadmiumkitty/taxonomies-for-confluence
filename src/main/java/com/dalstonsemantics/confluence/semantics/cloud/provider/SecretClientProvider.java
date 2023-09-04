package com.dalstonsemantics.confluence.semantics.cloud.provider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;

import org.springframework.stereotype.Component;

@Component
public class SecretClientProvider {
    
    private DefaultAzureCredential credential;
    private ConcurrentMap<String, SecretClient> secretClients;

    public SecretClientProvider() {
        this.credential = new DefaultAzureCredentialBuilder().build();
        this.secretClients = new ConcurrentHashMap<>();
    }

    public SecretClient getSecretClient(String vaultUrl) {
        return secretClients.computeIfAbsent(vaultUrl, x -> {
            return new SecretClientBuilder()
                .vaultUrl(x)
                .credential(credential)
                .buildClient();
        });
    }
}
