package eu.ill.visa.web.graphql.types;


import java.util.List;

public class CloudResourceClassesResponse {
    private final boolean available;
    private final List<String> resourceClasses;

    public static CloudResourceClassesResponse Unavailable() {
        return new CloudResourceClassesResponse(false, null);
    }

    public static CloudResourceClassesResponse Response(final List<String> resourceClasses) {
        return new CloudResourceClassesResponse(true, resourceClasses);
    }

    private CloudResourceClassesResponse(boolean available, List<String> resourceClasses) {
        this.available = available;
        this.resourceClasses = resourceClasses;
    }

    public boolean isAvailable() {
        return available;
    }

    public List<String> getResourceClasses() {
        return resourceClasses;
    }
}
