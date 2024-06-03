package eu.ill.visa.cloud.providers.openstack;

import eu.ill.visa.cloud.domain.*;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.http.HttpClient;
import eu.ill.visa.cloud.http.HttpResponse;
import eu.ill.visa.cloud.providers.CloudProvider;
import eu.ill.visa.cloud.providers.openstack.converters.LimitConverter;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static eu.ill.visa.cloud.helpers.JsonHelper.parseObject;
import static eu.ill.visa.cloud.http.HttpMethod.*;
import static jakarta.json.Json.createArrayBuilder;
import static jakarta.json.Json.createObjectBuilder;
import static java.lang.String.format;
import static java.util.stream.Collectors.toUnmodifiableList;

public class OpenStackProvider implements CloudProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenStackProvider.class);

    private static final String                         HEADER_X_AUTH_TOKEN    = "X-Auth-Token";
    private final        HttpClient                     httpClient;
    private final        OpenStackProviderConfiguration configuration;

    private final OpenStackIdentityProvider identityProvider;
    private final OpenStackImageProvider imageProvider;
    private final OpenStackComputeProvider computeProvider;

    public OpenStackProvider(final HttpClient httpClient, final OpenStackProviderConfiguration configuration) {
        this.httpClient = httpClient;
        this.configuration = configuration;
        this.identityProvider = new OpenStackIdentityProvider(this.configuration);
        this.imageProvider = new OpenStackImageProvider(this.configuration, this.identityProvider);
        this.computeProvider = new OpenStackComputeProvider(this.configuration, this.identityProvider);
    }

    public OpenStackProviderConfiguration getConfiguration() {
        return configuration;
    }

    private String authenticate() throws CloudException {
        return this.identityProvider.authenticate();
    }

    private Map<String, String> buildDefaultHeaders(final String authToken) {
        return new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, authToken);
        }};
    }

    @Override
    public List<CloudImage> images() throws CloudException {
        return this.imageProvider.images();
    }

    @Override
    public CloudImage image(final String id) throws CloudException {
        return this.imageProvider.image(id);
    }

    @Override
    public List<CloudFlavour> flavors() throws CloudException {
        return this.computeProvider.flavors();
    }

    @Override
    public CloudFlavour flavor(final String id) throws CloudException {
        return this.computeProvider.flavor(id);
    }

    @Override
    public List<CloudInstanceIdentifier> instanceIdentifiers() throws CloudException {
        return this.computeProvider.instanceIdentifiers();
    }

    @Override
    public List<CloudInstance> instances() throws CloudException {
        return this.computeProvider.instances();
    }

    @Override
    public CloudInstance instance(final String id) throws CloudException {
        return this.computeProvider.instance(id);
    }

    @Override
    public String ip(final String id) throws CloudException {
        final CloudInstance instance = this.instance(id);
        return instance.getAddress();
    }

    @Override
    public void rebootInstance(String id) throws CloudException {
        this.computeProvider.rebootInstance(id);
    }

    @Override
    public void startInstance(String id) throws CloudException {
        this.computeProvider.startInstance(id);
    }

    @Override
    public void shutdownInstance(String id) throws CloudException {
        this.computeProvider.shutdownInstance(id);
    }

    @Override
    public void updateSecurityGroups(String id, List<String> securityGroupNames) throws CloudException {
        final String url = format("%s/v2/servers/%s/os-security-groups", configuration.getComputeEndpoint(), id);
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            logger.warn("Could not get security groups from the server with id {} and response {}", id, response.getBody());
        }
        final JsonArray currentSecurityGroups = parseObject(response.getBody()).getJsonArray("security_groups");
        final List<String> currentSecurityGroupNames = currentSecurityGroups.stream().map(jsonValue -> jsonValue.asJsonObject().getString("name")).collect(toUnmodifiableList());
        List<String> securityGroupNamesToRemove = currentSecurityGroupNames.stream().filter(name -> !securityGroupNames.contains(name)).collect(toUnmodifiableList());
        List<String> securityGroupNamesToAdd = securityGroupNames.stream().filter(name -> !currentSecurityGroupNames.contains(name)).collect(toUnmodifiableList());

        logger.info("Updating instance {} security groups: removing [{}] and adding [{}]", id, String.join(", ", securityGroupNamesToRemove), String.join(", ", securityGroupNamesToAdd));

        // Remove obsolete security groups one by one
        for (String securityGroupName : securityGroupNamesToRemove) {
            final JsonObjectBuilder name = createObjectBuilder().add("name", securityGroupName);
            final String json = createObjectBuilder().add("removeSecurityGroup", name).build().toString();
            final String removeActionUrl = format("%s/v2/servers/%s/action", configuration.getComputeEndpoint(), id);
            final HttpResponse removeActionResponse = httpClient.sendRequest(removeActionUrl, POST, headers, json);
            if (!removeActionResponse.isSuccessful()) {
                logger.warn("Could not remove security group '{}' from the server id {} and response {}", securityGroupName, id, response.getBody());
            }
        }

        // Add missing security groups one by one
        for (String securityGroupName : securityGroupNamesToAdd) {
            final JsonObjectBuilder name = createObjectBuilder().add("name", securityGroupName);
            final String json = createObjectBuilder().add("addSecurityGroup", name).build().toString();
            final String addActionUrl = format("%s/v2/servers/%s/action", configuration.getComputeEndpoint(), id);
            final HttpResponse addActionResponse = httpClient.sendRequest(addActionUrl, POST, headers, json);
            if (!addActionResponse.isSuccessful()) {
                logger.warn("Could not add security group '{}' to the server with id {} and response {}", securityGroupName, id, response.getBody());
            }
        }
    }

    @Override
    public CloudInstance createInstance(final String name,
                                        final String imageId,
                                        final String flavorId,
                                        final List<String> securityGroupNames,
                                        final CloudInstanceMetadata metadata,
                                        final String bootCommand) throws CloudException {

        final List<JsonObject> securityGroupObjects = securityGroupNames.stream().map(securityGroupName ->
            createObjectBuilder().add("name", securityGroupName).build()).collect(toUnmodifiableList());

        final JsonArrayBuilder securityGroupsBuilder = createArrayBuilder();
        securityGroupObjects.forEach(securityGroupsBuilder::add);
        final JsonArray securityGroups = securityGroupsBuilder.build();

        final JsonObject network = createObjectBuilder().add("uuid", configuration.getAddressProviderUUID()).build();
        final JsonArray networks = createArrayBuilder().add(network).build();

        final JsonObjectBuilder metadataNodes = createObjectBuilder();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            metadataNodes.add(entry.getKey(), entry.getValue());
        }

        final JsonObjectBuilder server = createObjectBuilder();
        server.add("name", name);
        server.add("imageRef", imageId);
        server.add("flavorRef", flavorId);
        server.add("security_groups", securityGroups);
        server.add("networks", networks);
        server.add("metadata", metadataNodes);
        if (bootCommand != null) {
            final Base64.Encoder encoder = Base64.getEncoder();
            server.add("user_data", encoder.encodeToString(bootCommand.getBytes()));
        }
        final String data = createObjectBuilder().add("server", server).build().toString();
        final String url = format("%s/v2/servers", configuration.getComputeEndpoint());
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);

        final HttpResponse response = httpClient.sendRequest(url, POST, headers, data);
        if (!response.isSuccessful()) {
            throw new CloudException(format("Could not create server with name %s and response %s ", name, response.getBody()));
        }
        final JsonObject result = parseObject(response.getBody()).getJsonObject("server");
        final String id = result.getString("id");
        return this.instance(id);
    }

    @Override
    public void deleteInstance(String id) throws CloudException {
        final String url = format("%s/v2/servers/%s", configuration.getComputeEndpoint(), id);
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, DELETE, headers);
        if (!response.isSuccessful()) {
            throw new CloudException(format("Could not delete server with id %s and response %s", id, response.getBody()));
        }
    }

    @Override
    public CloudLimit limits() throws CloudException {
        final String url = format("%s/v2/limits", configuration.getComputeEndpoint());
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            return null;
        }
        final JsonObject results = parseObject(response.getBody());
        return LimitConverter.fromJson(results);
    }

    @Override
    public List<String> securityGroups() throws CloudException {
        final String url = format("%s/v2.0/security-groups", configuration.getNetworkEndpoint());
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            logger.warn("Could not get security groups: {}", response.getBody());
            return new ArrayList<>();
        }
        final JsonArray currentSecurityGroups = parseObject(response.getBody()).getJsonArray("security_groups");
        return currentSecurityGroups.stream().map(jsonValue -> jsonValue.asJsonObject().getString("name"))
            .distinct()
            .sorted(String::compareToIgnoreCase)
            .collect(toUnmodifiableList());
    }

}
