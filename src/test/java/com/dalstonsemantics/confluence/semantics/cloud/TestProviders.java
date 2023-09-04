package com.dalstonsemantics.confluence.semantics.cloud;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.azure.core.http.rest.PagedIterable;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.queue.QueueClient;
import com.dalstonsemantics.confluence.semantics.cloud.provider.BlobClientProvider;
import com.dalstonsemantics.confluence.semantics.cloud.provider.CryptographyClientProvider;
import com.dalstonsemantics.confluence.semantics.cloud.provider.LocalDateTimeProvider;
import com.dalstonsemantics.confluence.semantics.cloud.provider.QueueClientProvider;
import com.dalstonsemantics.confluence.semantics.cloud.provider.SecretClientProvider;
import com.dalstonsemantics.confluence.semantics.cloud.provider.UUIDProvider;

@TestConfiguration
public class TestProviders {

    @Bean
    @Primary
    public CryptographyClientProvider createMockCryptographyClientProvider() {

        String sharedSecret = "XXXXX-XXXXX-XXXXX-XXXXX-XXXXX-XXXXX";
        String keyIdentifier = "https://tfc.vault.azure.net/keys/tfc-atlassian-shared-secret/fcc7aabb9c9849bdb8926488e9631f35";

        EncryptResult encryptResult = new EncryptResult(sharedSecret.getBytes(StandardCharsets.UTF_8), EncryptionAlgorithm.RSA_OAEP_256, keyIdentifier);
        DecryptResult decryptResult = new DecryptResult(sharedSecret.getBytes(StandardCharsets.UTF_8), EncryptionAlgorithm.RSA_OAEP_256, keyIdentifier);

        CryptographyClient mockCryptographyClient = Mockito.mock(CryptographyClient.class);
        Mockito.when(mockCryptographyClient.encrypt(Mockito.any(EncryptionAlgorithm.class), Mockito.any())).thenReturn(encryptResult);
        Mockito.when(mockCryptographyClient.decrypt(Mockito.any(EncryptionAlgorithm.class), Mockito.any())).thenReturn(decryptResult);

        CryptographyClientProvider mockCryptographyClientProvider = Mockito.mock(CryptographyClientProvider.class);
        Mockito.when(mockCryptographyClientProvider.getCryptographyClient(Mockito.any())).thenReturn(mockCryptographyClient);

        return mockCryptographyClientProvider;
    }

    @Bean
    @Primary
    public SecretClientProvider createMockSecretClientProvider() {

        String secretName = "sparql-927294f7-0a9f-3d01-8120-b3ca3a45df38";
        String secretValue = "XXX";
        String vaultUrl = "https://tfc.vault.azure.net";

        SecretClient mockSecretClient = Mockito.mock(SecretClient.class);
        Mockito.when(mockSecretClient.getSecret(secretName)).thenReturn(new KeyVaultSecret(secretName, secretValue));

        SecretClientProvider mockSecretClientProvider = Mockito.mock(SecretClientProvider.class);
        Mockito.when(mockSecretClientProvider.getSecretClient(Mockito.eq(vaultUrl))).thenReturn(mockSecretClient);

        return mockSecretClientProvider;
    }

    @Bean
    @Primary
    public QueueClientProvider createMockQueueClientProvider() {

        PagedIterable pagedIterable = Mockito.mock(PagedIterable.class);
        Mockito.when(pagedIterable.iterator()).thenReturn(Arrays.asList().iterator());
        
        QueueClient mockQueueClient = Mockito.mock(QueueClient.class);
        Mockito.when(mockQueueClient.receiveMessages(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(pagedIterable);

        QueueClientProvider mockQueueClientProvider = Mockito.mock(QueueClientProvider.class);
        Mockito.when(mockQueueClientProvider.getQueueClient(Mockito.any())).thenReturn(mockQueueClient);

        return mockQueueClientProvider;
    }

    @Bean
    @Primary
    public BlobClientProvider createMockBlobClientProvider() {

        BlobProperties mockBlobProperties = Mockito.mock(BlobProperties.class);
        Mockito.when(mockBlobProperties.getContentType()).thenReturn("text/turtle");

        BlobInputStream blobInputStream = Mockito.mock(BlobInputStream.class);

        BlobClient mockBlobClient = Mockito.mock(BlobClient.class);
        Mockito.when(mockBlobClient.openInputStream()).thenReturn(blobInputStream);
        Mockito.when(mockBlobClient.getProperties()).thenReturn(mockBlobProperties);

        BlobClientProvider mockBlobClientProvider = Mockito.mock(BlobClientProvider.class);
        Mockito.when(mockBlobClientProvider.getBlobClient(Mockito.any(), Mockito.any())).thenReturn(mockBlobClient);

        return mockBlobClientProvider;
    }

    @Bean
    @Primary
    public UUIDProvider createMockUUIDProvider() {

        List<UUID> uuids = IntStream.range(1, 1000).boxed().map(i -> UUID.fromString(String.format("b7279f95-1820-4582-8088-f7d065f%05d", i))).collect(Collectors.toList());

        UUIDProvider mockProvider = Mockito.mock(UUIDProvider.class);
        Mockito.when(mockProvider.randomUUID()).thenReturn(
            UUID.fromString("b7279f95-1820-4582-8088-f7d065fd116d"),
            uuids.toArray(new UUID[0]));
        return mockProvider;
    }

    @Bean
    @Primary
    public LocalDateTimeProvider createMockLocalDateTimeProvider() {

        LocalDateTimeProvider mockLocalDateTimeProvider = Mockito.mock(LocalDateTimeProvider.class);
        Mockito.when(mockLocalDateTimeProvider.nowInUTC()).thenReturn(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));
        return mockLocalDateTimeProvider;
    }
}
