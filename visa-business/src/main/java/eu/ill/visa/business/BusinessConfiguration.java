package eu.ill.visa.business;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class BusinessConfiguration {

    private Integer numberInstanceActionThreads = 8;

    private List<NotificationConfiguration> notificationConfiguration = new ArrayList<>();

    private InstanceConfiguration instanceConfiguration;

    @NotNull
    @Valid
    private SignatureConfiguration signatureConfiguration;

    private SecurityGroupServiceClientConfiguration securityGroupServiceClientConfiguration;

    private String rootURL;

    public BusinessConfiguration() {

    }

    public BusinessConfiguration(InstanceConfiguration instanceConfiguration) {
        this.instanceConfiguration = instanceConfiguration;
    }

    @JsonProperty
    @NotNull
    @Valid
    public Integer getNumberInstanceActionThreads() {
        return numberInstanceActionThreads;
    }

    @JsonProperty("notifications")
    @NotNull
    @Valid
    public List<NotificationConfiguration> getNotificationConfiguration() {
        return notificationConfiguration;
    }

    public void setNotificationConfiguration(List<NotificationConfiguration> notificationConfiguration) {
        this.notificationConfiguration = notificationConfiguration;
    }

    @JsonProperty("signature")
    public SignatureConfiguration getSignatureConfiguration() {
        return signatureConfiguration;
    }

    @JsonProperty("instance")
    @NotNull
    @Valid
    public InstanceConfiguration getInstanceConfiguration() {
        return instanceConfiguration;
    }

    @JsonProperty("securityGroupServiceClient")
    @NotNull
    @Valid
    public SecurityGroupServiceClientConfiguration getSecurityGroupServiceClientConfiguration() {
        return securityGroupServiceClientConfiguration;
    }

    @JsonProperty("rootURL")
    @NotNull
    @Valid
    public String getRootURL() {
        return this.rootURL;
    }
}
