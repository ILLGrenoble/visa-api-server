package eu.ill.visa.web.rest.converters.dozer;


import com.github.dozermapper.core.DozerConverter;
import org.joda.time.DateTime;

public class DateTimeCustomConverter extends DozerConverter<DateTime, DateTime> {

    public DateTimeCustomConverter() {
        super(DateTime.class, DateTime.class);
    }

    @Override
    public DateTime convertTo(DateTime source, DateTime destination) {
        return convertDateTime(source);
    }

    @Override
    public DateTime convertFrom(DateTime source, DateTime destination) {
        return convertDateTime(source);
    }

    /**
     * Converts a <code>DateTime</code> to a new instance.
     */
    private DateTime convertDateTime(DateTime source) {
        return source != null ? new DateTime(source) : null;
    }

}
