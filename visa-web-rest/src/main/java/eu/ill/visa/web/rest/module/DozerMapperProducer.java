package eu.ill.visa.web.rest.module;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.io.InputStream;

import static java.lang.ClassLoader.getSystemResourceAsStream;

@ApplicationScoped
public class DozerMapperProducer {

    @Produces
    public Mapper getMapper() {
        final InputStream mappings = getSystemResourceAsStream("dozer/mappings.xml");
        return DozerBeanMapperBuilder.create().withXmlMapping(() -> mappings).build();
    }
}
