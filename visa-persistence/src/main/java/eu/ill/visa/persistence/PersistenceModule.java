package eu.ill.visa.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;

import java.util.Properties;

public class PersistenceModule extends AbstractModule {

    private final String jpaUnit;

    private final Properties properties;

    public PersistenceModule(String jpaUnit) {
        this(jpaUnit, null);
    }

    public PersistenceModule(Properties properties) {
        this("visa", properties);
    }

    public PersistenceModule(String jpaUnit, Properties properties) {
        this.jpaUnit = jpaUnit;
        this.properties = properties;
    }

    @Override
    protected void configure() {
        final JpaPersistModule module = new JpaPersistModule(jpaUnit);
        if (properties != null) {
            module.properties(properties);
        }
        install(module);
        bind(DatabaseInitializer.class).asEagerSingleton();
    }

    @Singleton
    private static class DatabaseInitializer {
        @Inject
        public DatabaseInitializer(final PersistService service) {
            service.start();
        }
    }
}
