package eu.ill.visa.web.graphqlxx.types;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

public class FlavourType {

    @AdaptToScalar(Scalar.Int.class)
    private Long id;
    private String name;
    private Integer memory;
    private Float cpu;
    private String computeId;
    private CloudFlavourType cloudFlavour;
    private CloudClientType cloudClient;

    public FlavourType() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public Float getCpu() {
        return cpu;
    }

    public void setCpu(Float cpu) {
        this.cpu = cpu;
    }

    public String getComputeId() {
        return computeId;
    }

    public void setComputeId(String computeId) {
        this.computeId = computeId;
    }

    public CloudFlavourType getCloudFlavour() {
        return cloudFlavour;
    }

    public void setCloudFlavour(CloudFlavourType cloudFlavour) {
        this.cloudFlavour = cloudFlavour;
    }

    public CloudClientType getCloudClient() {
        return cloudClient;
    }

    public void setCloudClient(CloudClientType cloudClient) {
        this.cloudClient = cloudClient;
    }
}
