package eu.ill.visa.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

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

        private final Provider<EntityManager> entityManagerProvider;
        @Inject
        public DatabaseInitializer(final PersistService service, final Provider<EntityManager> entityManagerProvider) {
            this.entityManagerProvider = entityManagerProvider;
            service.start();

            // Run migrations
            this.executeSQL("migrations/2.1.2.sql");
        }

        private void executeSQL(String filename) {
            final String sql = getFileAsString(filename);

            EntityManager em = this.entityManagerProvider.get();
            em.clear();
            Session session = em.unwrap(Session.class);
            session.doWork(connection -> {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(sql);
                } catch (Exception exception) {
                    System.out.println(exception.getMessage());
                }
            });
        }

        private String getFileAsString(String fileName) {
            final InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
            return null;
        }
    }
}
