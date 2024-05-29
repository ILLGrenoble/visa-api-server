package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.domain.CloudFlavour;
import org.eclipse.microprofile.graphql.Type;

@Type("CloudFlavour")
public class CloudFlavourType {

    private final String id;
    private final String name;
    private final Integer cpus;
    private final Integer disk;
    private final Integer ram;

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
