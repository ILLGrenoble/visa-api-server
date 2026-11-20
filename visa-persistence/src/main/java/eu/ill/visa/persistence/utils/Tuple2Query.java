package eu.ill.visa.persistence.utils;


import eu.ill.visa.persistence.models.Tuple2;
import jakarta.persistence.TypedQuery;

import java.util.List;

public record Tuple2Query<X, Y>(TypedQuery<Object[]> query, Class<X> xClass, Class<Y> yClass) {

    public List<Tuple2<X, Y>> getResultList() throws Tuple2QueryException {
        List<Object[]> results = query.getResultList();
        return results.stream().map(objects -> {
            try {
                return convertObjectArray(objects);
            } catch (Tuple2QueryException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    private Tuple2<X, Y> convertObjectArray(Object[] objects) throws Tuple2QueryException {
        if (objects.length != 2) {
            throw new Tuple2QueryException("Tuple2Query did not return 2 values");
        }

        if (objects[0] != null && !this.xClass.isInstance(objects[0])) {
            throw new Tuple2QueryException("Objects[0] is not of type " + xClass.getCanonicalName());
        }

        if (objects[1] != null && !this.yClass.isInstance(objects[1])) {
            throw new Tuple2QueryException("Objects[1] is not of type " + yClass.getCanonicalName());
        }

        return new Tuple2<>(this.xClass.cast(objects[0]), this.yClass.cast(objects[1]));
    }

    public static class Tuple2QueryException extends Exception {
        Tuple2QueryException(String message) {
            super(message);
        }
    }

}
