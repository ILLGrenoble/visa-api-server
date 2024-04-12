package eu.ill.visa.business.services;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import eu.ill.visa.core.domain.ApplicationCredential;
import eu.ill.visa.persistence.repositories.ApplicationCredentialRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.bouncycastle.crypto.generators.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Transactional
@Singleton
public class ApplicationCredentialService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationCredentialService.class);

    private final ApplicationCredentialRepository repository;

    @Inject
    public ApplicationCredentialService(final ApplicationCredentialRepository repository) {
        this.repository = repository;
    }

    public List<ApplicationCredential> getAll() {
        return this.repository.getAll();
    }

    public ApplicationCredential getById(Long id) {
        return this.repository.getById(id);
    }

    public ApplicationCredential getByApplicationIdAndApplicationSecret(String applicationId, String applicationSecret) {
        ApplicationCredential applicationCredential = this.repository.getByApplicationId(applicationId);
        if (applicationCredential != null) {
            String applicationSecretHash = this.hashText(applicationSecret, applicationCredential.getSalt());

            if (applicationSecretHash.equals(applicationCredential.getApplicationSecret())) {
                return applicationCredential;

            } else {
                logger.warn("Secret incorrect for application credential {}", applicationCredential.getName());
            }

        } else {
            logger.warn("Application credential with applicationId {} does not exist", applicationId);
        }

        return null;
    }

    public void delete(ApplicationCredential applicationCredential) {
        applicationCredential.setDeletedAt(new Date());
        this.save(applicationCredential);
    }

    public void save(@NotNull ApplicationCredential applicationCredential) {
        this.repository.save(applicationCredential);
    }

    public ApplicationCredential create(String name) {
        String applicationId = this.createApplicationId();
        String applicationSecret = RandomStringUtils.randomAlphanumeric(72);
        String salt = RandomStringUtils.randomAlphanumeric(16);

        String applicationSecretHash = this.hashText(applicationSecret, salt);
        ApplicationCredential applicationCredential = new ApplicationCredential(name, applicationId, applicationSecret);

        ApplicationCredential hashedApplicationCredential = new ApplicationCredential(name, salt, applicationId, applicationSecretHash);
        this.save(hashedApplicationCredential);

        applicationCredential.setId(hashedApplicationCredential.getId());
        return applicationCredential;
    }

    private String hashText(String text, String salt) {
        byte[] textBytes = text.getBytes();
        byte[] saltBytes = salt.getBytes();

        byte[] encodedHash = BCrypt.generate(textBytes, saltBytes, 4);
        return this.bytesToHex(encodedHash);
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String createApplicationId() {
        boolean exists;
        String applicationId;
        do {
            applicationId = UUID.randomUUID().toString().replace("-", "");

            exists = this.repository.getByApplicationId(applicationId) != null;

        } while (exists);

        return applicationId;
    }
}
