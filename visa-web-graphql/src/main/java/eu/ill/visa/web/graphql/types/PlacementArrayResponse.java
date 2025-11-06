package eu.ill.visa.web.graphql.types;


import java.util.List;

public class PlacementArrayResponse<T> {
    private final boolean available;
    private final List<T> data;

    public static <T> PlacementArrayResponse<T> Unavailable() {
        return new PlacementArrayResponse<T>(false, null);
    }

    public static <T> PlacementArrayResponse<T> Response(final List<T> data) {
        return new PlacementArrayResponse<T>(true, data);
    }

    private PlacementArrayResponse(boolean available, List<T> data) {
        this.available = available;
        this.data = data;
    }

    public boolean isAvailable() {
        return available;
    }

    public List<T> getData() {
        return data;
    }
}
