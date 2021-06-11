package eu.ill.visa.web.bundles.graphql.scalars;

import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DateScalar extends GraphQLScalarType {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public DateScalar() {
        super("Date", "Date type", new Coercing() {
            @Override
            public String serialize(Object o) {
                if (o instanceof Date) {
                    return formatter.format((Date) o);
                }
                return null;
            }

            @Override
            public Date parseValue(Object o) {
                if (o instanceof String)  {
                    return new Date(Long.parseLong(o.toString()));
                }
                return null;
            }

            @Override
            public Date parseLiteral(Object o) {
                if (o instanceof String)  {
                    return new Date(Long.parseLong(o.toString()));
                }
                return null;
            }
        });
    }
}
