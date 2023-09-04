package com.dalstonsemantics.confluence.semantics.cloud.provider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BlobClientProvider {

    private DefaultAzureCredential credential;
    private BlobServiceClient blobServiceClient;
    private ConcurrentMap<String, BlobContainerClient> blobContainerClients;

    public BlobClientProvider(@Value("${addon.blobs.blob-service-client-endpoint-url}") String endpointUrl) {
        this.credential = new DefaultAzureCredentialBuilder().build();
        this.blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(endpointUrl)
                .credential(credential)
                .buildClient();
        this.blobContainerClients = new ConcurrentHashMap<>();
    }

    public BlobContainerClient getBlobContainerClient(String containerName) {
        return blobContainerClients.computeIfAbsent(containerName, x -> {
            return blobServiceClient.getBlobContainerClient(x);
        });
    }

    public BlobClient getBlobClient(String containerName, String blobName) {
        return getBlobContainerClient(containerName).getBlobClient(blobName);
    }

}
