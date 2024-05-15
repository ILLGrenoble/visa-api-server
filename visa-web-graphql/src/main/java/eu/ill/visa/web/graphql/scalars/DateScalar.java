package eu.ill.visa.web.graphql.scalars;

import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DateScalar {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public static final GraphQLScalarType DATE = GraphQLScalarType.newScalar()
        .name("Date")
        .description("Date type")
        .coercing(new Coercing() {

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
        })
        .build();
}
