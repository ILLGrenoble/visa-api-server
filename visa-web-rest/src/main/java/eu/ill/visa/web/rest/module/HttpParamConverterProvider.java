package eu.ill.visa.web.rest.module;

import eu.ill.visa.core.entity.*;

import eu.ill.visa.web.rest.converters.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
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
    private final BookingRequestParamConverter bookingRequestParamConverter;
    private final BookingTokenParamConverter bookingTokenParamConverter;

    @Inject
    public HttpParamConverterProvider(final InstrumentParamConverter instrumentParamConverter,
                                      final UserParamConverter userParamConverter,
                                      final ExperimentParamConverter experimentParamConverter,
                                      final ImageParamConverter imageParamConverter,
                                      final FlavourParamConverter flavourParamConverter,
                                      final PlanParamConverter planParamConverter,
                                      final InstanceParamConverter instanceParamConverter,
                                      final InstanceMemberParamConverter instanceMemberParamConverter,
                                      final BookingRequestParamConverter bookingRequestParamConverter,
                                      final BookingTokenParamConverter bookingTokenParamConverter) {
        this.instrumentParamConverter = instrumentParamConverter;
        this.userParamConverter = userParamConverter;
        this.experimentParamConverter = experimentParamConverter;
        this.imageParamConverter = imageParamConverter;
        this.flavourParamConverter = flavourParamConverter;
        this.planParamConverter = planParamConverter;
        this.instanceParamConverter = instanceParamConverter;
        this.instanceMemberParamConverter = instanceMemberParamConverter;
        this.bookingRequestParamConverter = bookingRequestParamConverter;
        this.bookingTokenParamConverter = bookingTokenParamConverter;
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

        if (BookingRequest.class.isAssignableFrom(rawType)) {
            return (ParamConverter<T>) bookingRequestParamConverter;
        }

        if (BookingToken.class.isAssignableFrom(rawType)) {
            return (ParamConverter<T>) bookingTokenParamConverter;
        }

        return null;
    }


}
