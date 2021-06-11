package eu.ill.visa.vdi.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

@Singleton
public class SignatureService {

    private static final Logger logger              = LoggerFactory.getLogger(SignatureService.class);
    public static        String SIGNATURE_ALGORITHM = "SHA256withRSA";
    public static        String KEY_ALGORITHM       = "RSA";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private String privateKey;

    private String publicKey;

    @Inject
    public SignatureService(final String privateKeyPath,
                            final String publicKeyPath) {
        this.privateKey = privateKeyPath;
        this.publicKey = publicKeyPath;
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
            final byte[] bytes = payload.getBytes(UTF_8);
            final byte[] signature = createSignature(bytes);
            return encodeBase64(signature);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException |
            InvalidKeyException | IOException | SignatureException exception) {
            logger.error("Could not create signature: {}", exception.getMessage());
            return null;
        }
    }

    /**
     * Verify a signature a verify the payload has not been tampered with
     */
    public boolean verify(final String payload, final String signature) {
        final byte[] decodedSignature = decodeBase64(signature);
        try {
            return verifySignature(payload, decodedSignature);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException |
            IOException | InvalidKeyException | SignatureException exception) {
            logger.error("Could not verify signature: {}", exception.getMessage());
            return false;
        }
    }

    private boolean verifySignature(final String payload, final byte[] signature) throws InvalidKeySpecException,
        NoSuchAlgorithmException, IOException, InvalidKeyException, SignatureException {
        final Signature signatureInstance = createSignatureInstance();
        final PublicKey key = createPublicKey();
        signatureInstance.initVerify(key);
        signatureInstance.update(payload.getBytes());
        return signatureInstance.verify(signature);
    }

    private byte[] createSignature(final byte[] payload) throws InvalidKeySpecException,
        NoSuchAlgorithmException, IOException, InvalidKeyException, SignatureException {

        final PrivateKey key = createPrivateKey();
        final Signature signatureInstance = createSignatureInstance();

        signatureInstance.initSign(key);
        signatureInstance.update(payload);

        return signatureInstance.sign();
    }

    private String encodeBase64(final byte[] payload) {
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(payload);
    }

    private byte[] decodeBase64(final String payload) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(payload);
    }

    private KeyFactory createKeyFactoryInstance() throws NoSuchAlgorithmException {
        return KeyFactory.getInstance(KEY_ALGORITHM);
    }

    private Signature createSignatureInstance() throws NoSuchAlgorithmException {
        return Signature.getInstance(SIGNATURE_ALGORITHM);
    }

    private byte[] readKey(final String inputFilePath) throws IOException {
        try (final PemReader reader = new PemReader(new FileReader(inputFilePath))) {
            final PemObject object = reader.readPemObject();
            return object.getContent();
        }
    }

    private PrivateKey createPrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        final byte[] bytes = readKey(privateKey);
        final PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(bytes);
        final KeyFactory keyFactory = createKeyFactoryInstance();
        return keyFactory.generatePrivate(encodedKeySpec);
    }

    private PublicKey createPublicKey() throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        final byte[] bytes = readKey(publicKey);
        final X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(bytes);
        final KeyFactory keyFactory = createKeyFactoryInstance();
        return keyFactory.generatePublic(encodedKeySpec);
    }

}
