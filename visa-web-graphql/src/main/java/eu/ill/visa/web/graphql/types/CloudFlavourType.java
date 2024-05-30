package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.domain.CloudFlavour;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("CloudFlavour")
public class CloudFlavourType {

    private final @NotNull String id;
    private final @NotNull String name;
    private final @NotNull Integer cpus;
    private final @NotNull Integer disk;
    private final @NotNull Integer ram;

    public CloudFlavourType(final CloudFlavour cloudFlavour) {
        this.id = cloudFlavour.getId();
        this.name = cloudFlavour.getName();
        this.cpus = cloudFlavour.getCpus();
        this.disk = cloudFlavour.getDisk();
        this.ram = cloudFlavour.getRam();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getCpus() {
        return cpus;
    }

    public Integer getDisk() {
        return disk;
    }

    public Integer getRam() {
        return ram;
    }
}
