package eu.ill.visa.web;

import com.google.inject.Provides;
import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.visa.business.BusinessConfiguration;
import eu.ill.visa.cloud.CloudConfiguration;
import eu.ill.visa.persistence.PersistenceModule;
import eu.ill.visa.scheduler.SchedulerConfiguration;
import eu.ill.visa.security.SecurityConfiguration;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.web.bundles.graphql.GraphQLWebServletConfiguration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import ru.vyarus.dropwizard.guice.module.support.DropwizardAwareModule;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static java.lang.ClassLoader.getSystemResourceAsStream;

public class WebModule extends DropwizardAwareModule<WebConfiguration> {

    @Override
    protected void configure() {
        configuration();
        environment();
        bootstrap();
        bindServices();
        configureDatabase();
    }

    private Properties createDatabasePropertiesFromDataSource(final DataSourceFactory dataSourceFactory) {
        final Properties properties = new Properties();
        properties.put("jakarta.persistence.jdbc.driver", dataSourceFactory.getDriverClass());
        properties.put("jakarta.persistence.jdbc.url", dataSourceFactory.getUrl());
        properties.put("jakarta.persistence.jdbc.user", dataSourceFactory.getUser());
        properties.put("jakarta.persistence.jdbc.password", dataSourceFactory.getPassword());

        for (Map.Entry<String, String> entry : dataSourceFactory.getProperties().entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        return properties;
    }

    private void configureDatabase() {
        final DataSourceFactory dataSourceFactory = this.configuration().getDataSourceFactory();
        final ManagedDataSource managedDataSource = dataSourceFactory.build(environment().metrics(), "database");
        final Properties properties = createDatabasePropertiesFromDataSource(dataSourceFactory);

        install(new PersistenceModule("visa", properties));

        environment().lifecycle().manage(managedDataSource);
    }

    private void bindServices() {

    }

    @Provides
    public SecurityConfiguration providesSecurityConfiguration() {
        return this.configuration().getSecurityConfiguration();
    }

    @Provides
    public CloudConfiguration providesCloudConfiguration() {
        return this.configuration().getCloudConfiguration();
    }

    @Provides
    public VirtualDesktopConfiguration providesVirtualDesktopConfiguration() {
        return this.configuration().getVirtualDesktopConfiguration();
    }

    @Provides
    public SchedulerConfiguration providesSchedulerConfiguration() {
        return this.configuration().getSchedulerConfiguration();
    }

    @Provides
    public BusinessConfiguration providesBusinessConfiguration() {
        return this.configuration().getBusinessConfiguration();
    }

    @Provides
    public GraphQLWebServletConfiguration providesGraphQLWebServletConfiguration() {
        return this.configuration().getGraphQLWebServletConfiguration();
    }

    @Provides
    public ClientConfiguration providesClientConfiguration() {
        return this.configuration().getClientConfiguration();
    }

    @Provides
    public DesktopConfiguration providesDesktopConfiguration() {
        return this.configuration().getClientConfiguration().getDesktopConfiguration();
    }

    @Provides
    public ManagedDataSource providesManagedDataSource() {
        DataSourceFactory dataSourceFactory = this.configuration().getDataSourceFactory();
        return dataSourceFactory.build(environment().metrics(), "VISA");
    }

    @Provides
    @ApplicationScoped
    public Mapper providesMapper() {
        final DozerBeanMapper mapper = new DozerBeanMapper();
        final InputStream mappings = getSystemResourceAsStream("dozer/mappings.xml");
        mapper.addMapping(mappings);
        return mapper;
    }

}
