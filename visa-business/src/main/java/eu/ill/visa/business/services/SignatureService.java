package eu.ill.visa.business.services;

import eu.ill.visa.business.SignatureConfiguration;
import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

@Startup
@Singleton
public class SignatureService {

    private static final Logger logger              = LoggerFactory.getLogger(SignatureService.class);
    public static        String SIGNATURE_ALGORITHM = "SHA256withRSA";
    public static        String KEY_ALGORITHM       = "RSA";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final String privateKeyPath;
    private final String publicKeyPath;

    @Inject
    public SignatureService(SignatureConfiguration signatureConfiguration) {
        this.privateKeyPath = signatureConfiguration.privateKeyPath().orElse("");
        this.publicKeyPath = signatureConfiguration.publicKeyPath().orElse("");

        if (privateKeyPath.isEmpty()) {
            logger.info("VISA not configured to generated signed VISA PAM token for remote desktop access");
        } else {
            logger.info("VISA configured to generated signed VISA PAM token for remote desktop access");
        }

        if (publicKeyPath.isEmpty()) {
            logger.info("VISA not configured to send VISA PAM public key to instances");
        } else {
            logger.info("VISA configured to send VISA PAM public key to instances");
        }
    }

    public String createSignature(String username) {
        final String payload = format("%s,%d", username, System.currentTimeMillis() / 1000);
        logger.debug("Signature payload: {}", payload);
        final String signature = this.sign(payload);
        return format("%s;%s", payload, signature);
    }

    /**
     * Sign payload
     */
    public String sign(final String payload) {
        try {
            final PrivateKey privateKey = createPrivateKey();
            final Signature signatureInstance = Signature.getInstance(SIGNATURE_ALGORITHM);
            signatureInstance.initSign(privateKey);
            signatureInstance.update(payload.getBytes(UTF_8));

            byte[] signatureBytes = signatureInstance.sign();

            return encodeBase64(signatureBytes);

        } catch (InvalidKeySpecException | NoSuchAlgorithmException |
            InvalidKeyException | IOException | SignatureException exception) {
            logger.error("Could not create signature: {}", exception.getMessage());
            return null;
        }
    }

    private String encodeBase64(final byte[] payload) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(payload);
    }

    private byte[] readKey(final String inputFilePath) throws IOException {
        try (final PemReader reader = new PemReader(new FileReader(inputFilePath))) {
            final PemObject object = reader.readPemObject();
            return object.getContent();
        }
    }

    private PrivateKey createPrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        final byte[] privateKeyBytes = this.readKey(this.privateKeyPath);
        final PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        final KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(encodedKeySpec);
    }

    public String readPublicKey()  {
        if (!publicKeyPath.isEmpty()) {
            try {
                return Files.readString(Paths.get(publicKeyPath));

            } catch (IOException e) {
                logger.error("Could not read public key from {}: {}", publicKeyPath, e.getMessage());
            }
        }
        return null;
    }

}
