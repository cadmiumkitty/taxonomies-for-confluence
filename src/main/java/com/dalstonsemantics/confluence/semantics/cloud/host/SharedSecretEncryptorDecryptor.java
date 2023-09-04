package com.dalstonsemantics.confluence.semantics.cloud.host;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.dalstonsemantics.confluence.semantics.cloud.provider.CryptographyClientProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SharedSecretEncryptorDecryptor {
    
    private CryptographyClientProvider cryptographyClientProvider;

    public SharedSecretEncryptorDecryptor(@Autowired CryptographyClientProvider cryptographyClientProvider) {
        this.cryptographyClientProvider = cryptographyClientProvider;
    }

    public String encrypt(String sharedSecretPlaintext, String keyIdentifier) {

        byte[] sharedSecretPlaintextBytes = sharedSecretPlaintext.getBytes(StandardCharsets.UTF_8);

        CryptographyClient cryptographyClient = cryptographyClientProvider.getCryptographyClient(keyIdentifier);
        EncryptResult encryptResult = cryptographyClient.encrypt(EncryptionAlgorithm.RSA_OAEP_256, sharedSecretPlaintextBytes);

        String sharedSecretCyphertextBase64 = Base64.getEncoder().encodeToString(encryptResult.getCipherText());

        return sharedSecretCyphertextBase64;
    }

    public String decrypt(String sharedSecretCyphertextBase64, String keyIdentifier) {

        byte[] sharedSecretCyphertextBytes = Base64.getDecoder().decode(sharedSecretCyphertextBase64);

        CryptographyClient cryptographyClient = cryptographyClientProvider.getCryptographyClient(keyIdentifier);
        DecryptResult decryptResult = cryptographyClient.decrypt(EncryptionAlgorithm.RSA_OAEP_256, sharedSecretCyphertextBytes);

        String sharedSecretCleartext = new String(decryptResult.getPlainText(), StandardCharsets.UTF_8);

        return sharedSecretCleartext;
    }
}
