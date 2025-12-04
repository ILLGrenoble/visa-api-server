package eu.ill.visa.business.services;

import eu.ill.visa.core.entity.BookingConfiguration;
import eu.ill.visa.persistence.repositories.BookingConfigurationRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import static java.util.Objects.requireNonNullElse;

@Transactional
@Singleton
public class BookingConfigurationService {

    private final BookingConfigurationRepository repository;

    @Inject
    public BookingConfigurationService(final BookingConfigurationRepository repository) {
        this.repository = repository;
    }

    public List<BookingConfiguration> getAll() {
        return this.repository.getAll();
    }

    public BookingConfiguration getById(Long id) {
        return this.repository.getById(id);
    }

    public BookingConfiguration getByCloudClientId(Long cloudClientId) {
        return this.repository.getByCloudClientId(requireNonNullElse(cloudClientId, -1L));
    }

    public void save(@NotNull BookingConfiguration bookingConfiguration) {
        this.repository.save(bookingConfiguration);
    }
}
