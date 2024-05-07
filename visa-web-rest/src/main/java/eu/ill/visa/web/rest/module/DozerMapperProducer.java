package eu.ill.visa.web.rest.module;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class DozerMapperProducer {

    @Produces
    public Mapper getMapper() {
        return DozerBeanMapperBuilder.create().withMappingFiles("dozer/mappings.xml").build();
    }
}
