package com.dalstonsemantics.confluence.semantics.cloud.host;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dalstonsemantics.confluence.semantics.cloud.provider.CryptographyClientProvider;

public class SharedSecretEncryptorDecryptorTest {
    
    @Test
    @Disabled
    public void shouldEncryptDecryptSharedSecret() {

        String sharedSecret = "XXXXX-XXXXX-XXXXX-XXXXX-XXXXX-XXXXX";
        String keyIdentifier = "https://tfc.vault.azure.net/keys/tfc-atlassian-shared-secret/fcc7aabb9c9849bdb8926488e9631f35";

        SharedSecretEncryptorDecryptor encryptorDecryptor = new SharedSecretEncryptorDecryptor(new CryptographyClientProvider());

        String sharedSecretCyphertextBase64 = encryptorDecryptor.encrypt(sharedSecret, keyIdentifier);
        String sharedSecretPlaintext = encryptorDecryptor.decrypt(sharedSecretCyphertextBase64, keyIdentifier);

        assertEquals(sharedSecret, sharedSecretPlaintext);
    }
}
