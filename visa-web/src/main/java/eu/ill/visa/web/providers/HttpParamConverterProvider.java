package eu.ill.visa.web.providers;

import eu.ill.visa.core.domain.*;
import eu.ill.visa.web.converters.http.*;

import com.google.inject.Inject;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class HttpParamConverterProvider implements ParamConverterProvider {

    private final InstrumentParamConverter instrumentParamConverter;
    private final UserParamConverter userParamConverter;
    private final ExperimentParamConverter experimentParamConverter;
    private final ImageParamConverter imageParamConverter;
    private final FlavourParamConverter flavourParamConverter;
    private final PlanParamConverter planParamConverter;
    private final InstanceParamConverter instanceParamConverter;
    private final InstanceMemberParamConverter instanceMemberParamConverter;

    @Inject
    public HttpParamConverterProvider(final InstrumentParamConverter instrumentParamConverter,
                                      final UserParamConverter userParamConverter,
                                      final ExperimentParamConverter experimentParamConverter,
                                      final ImageParamConverter imageParamConverter,
                                      final FlavourParamConverter flavourParamConverter,
                                      final PlanParamConverter planParamConverter,
                                      final InstanceParamConverter instanceParamConverter,
                                      final InstanceMemberParamConverter instanceMemberParamConverter) {
        this.instrumentParamConverter = instrumentParamConverter;
        this.userParamConverter = userParamConverter;
        this.experimentParamConverter = experimentParamConverter;
        this.imageParamConverter = imageParamConverter;
        this.flavourParamConverter = flavourParamConverter;
        this.planParamConverter = planParamConverter;
        this.instanceParamConverter = instanceParamConverter;
        this.instanceMemberParamConverter = instanceMemberParamConverter;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        if (Instrument.class.isAssignableFrom(rawType)) {
            return (ParamConverter<T>) instrumentParamConverter;
        }

        if (User.class.isAssignableFrom(rawType)) {
            return (ParamConverter<T>) userParamConverter;
        }

        if (Experiment.class.isAssignableFrom(rawType)) {
            return (ParamConverter<T>) experimentParamConverter;
        }

        if (Image.class.isAssignableFrom(rawType)) {
            return (ParamConverter<T>) imageParamConverter;
        }

        if (Flavour.class.isAssignableFrom(rawType)) {
            return (ParamConverter<T>) flavourParamConverter;
        }

        if (Plan.class.isAssignableFrom(rawType)) {
            return (ParamConverter<T>) planParamConverter;
        }

        if (Instance.class.isAssignableFrom(rawType)) {
            return (ParamConverter<T>) instanceParamConverter;
        }

        if (InstanceMember.class.isAssignableFrom(rawType)) {
            return (ParamConverter<T>) instanceMemberParamConverter;
        }

        return null;
    }


}
