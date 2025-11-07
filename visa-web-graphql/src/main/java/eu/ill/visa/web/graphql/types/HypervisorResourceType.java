package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.HypervisorResource;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import org.eclipse.microprofile.graphql.Type;

@Type("HypervisorResource")
public class HypervisorResourceType {

    private final String resourceClass;
    @AdaptToScalar(Scalar.Int.class)
    private final Long total;
    @AdaptToScalar(Scalar.Int.class)
    private final Long usage;

    public HypervisorResourceType(final HypervisorResource hypervisorResource) {
        this.resourceClass = hypervisorResource.getResourceClass();
        this.total = hypervisorResource.getTotal();
        this.usage = hypervisorResource.getUsage();
    }

    public String getResourceClass() {
        return resourceClass;
    }

    public Long getTotal() {
        return total;
    }

    public Long getUsage() {
        return usage;
    }
}
