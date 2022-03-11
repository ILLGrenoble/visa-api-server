package eu.ill.visa.cloud.providers.openstack;

import eu.ill.visa.cloud.domain.*;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.http.HttpClient;
import eu.ill.visa.cloud.http.HttpResponse;
import eu.ill.visa.cloud.providers.CloudProvider;
import eu.ill.visa.cloud.providers.openstack.converters.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.util.*;

import static eu.ill.visa.cloud.helpers.JsonHelper.parseObject;
import static eu.ill.visa.cloud.http.HttpMethod.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.toUnmodifiableList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

public class OpenStackProvider implements CloudProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenStackProvider.class);

    private static final String                         HEADER_X_SUBJECT_TOKEN = "X-Subject-Token";
    private static final String                         HEADER_X_AUTH_TOKEN    = "X-Auth-Token";
    private final        HttpClient                     httpClient;
    private final        OpenStackProviderConfiguration configuration;

    public OpenStackProvider(final HttpClient httpClient, final OpenStackProviderConfiguration configuration) {
        this.httpClient = httpClient;
        this.configuration = configuration;
    }

    private String authenticate() throws CloudException {
        final String data = AuthenticationConverter.toJson(configuration);
        final String url = format("%s/v3/auth/tokens", configuration.getIdentityEndpoint());
        final HttpResponse response = httpClient.sendRequest(url, POST, null, data);
        if (response.isSuccessful()) {
            return response.getHeaderIgnoreCase(HEADER_X_SUBJECT_TOKEN);
        }
        throw new CloudException("Error authenticating to openstack");
    }

    private Map<String, String> buildDefaultHeaders(final String authToken) {
        return new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, authToken);
        }};
    }

    @Override
    public List<CloudImage> images() throws CloudException {
        final String url = format("%s/v2/images", configuration.getImageEndpoint());
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            return null;
        }

        final JsonObject results = parseObject(response.getBody());
        final List<CloudImage> images = new ArrayList<>();
        for (final JsonValue imageValue : results.getJsonArray("images")) {
            final JsonObject cloudImage = (JsonObject) imageValue;
            final CloudImage image = ImageConverter.fromJson(cloudImage);
            images.add(image);
        }
        return images;
    }

    @Override
    public CloudImage image(final String id) throws CloudException {
        final String url = format("%s/v2/images/%s", configuration.getImageEndpoint(), id);
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            return null;
        }
        final JsonObject results = parseObject(response.getBody());
        return ImageConverter.fromJson(results);
    }

    @Override
    public List<CloudFlavour> flavors() throws CloudException {
        final String url = format("%s/v2/flavors/detail", configuration.getComputeEndpoint());
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            return null;
        }

        final JsonObject results = parseObject(response.getBody());
        final List<CloudFlavour> flavors = new ArrayList<>();
        for (final JsonValue flavorValue : results.getJsonArray("flavors")) {
            final JsonObject cloudFlavor = (JsonObject) flavorValue;
            final CloudFlavour flavor = FlavorConverter.fromJson(cloudFlavor);
            flavors.add(flavor);
        }
        return flavors;
    }

    @Override
    public CloudFlavour flavor(final String id) throws CloudException {
        final String url = format("%s/v2/flavors/%s", configuration.getComputeEndpoint(), id);
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            return null;
        }
        final JsonObject results = parseObject(response.getBody());
        return FlavorConverter.fromJson(results.getJsonObject("flavor"));
    }

    private List<CloudInstanceIdentifier> buildServerIdentifiersResponse(JsonObject response) {
        final List<CloudInstanceIdentifier> servers = new ArrayList<>();
        for (final JsonValue serverValue : response.getJsonArray("servers")) {
            final JsonObject cloudServerIdentifier = (JsonObject) serverValue;
            final CloudInstanceIdentifier server = InstanceIdentifierConverter.fromJson(cloudServerIdentifier);
            servers.add(server);
        }
        return servers;
    }

    private List<CloudInstance> buildServersResponse(JsonObject response) {
        final List<CloudInstance> servers = new ArrayList<>();
        for (final JsonValue serverValue : response.getJsonArray("servers")) {
            final JsonObject cloudServer = (JsonObject) serverValue;
            final CloudInstance server = InstanceConverter.fromJson(cloudServer, configuration.getAddressProvider());
            servers.add(server);
        }
        return servers;
    }

    @Override
    public List<CloudInstanceIdentifier> instanceIdentifiers() throws CloudException {
        final String url = format("%s/v2/servers", configuration.getComputeEndpoint());
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            return null;
        }
        final JsonObject results = parseObject(response.getBody());
        return buildServerIdentifiersResponse(results);
    }

    @Override
    public List<CloudInstance> instances() throws CloudException {
        final String url = format("%s/v2/servers/detail", configuration.getComputeEndpoint());
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            return null;
        }
        final JsonObject results = parseObject(response.getBody());
        return buildServersResponse(results);
    }

    @Override
    public CloudInstance instance(final String id) throws CloudException {
        final String url = format("%s/v2/servers/%s", configuration.getComputeEndpoint(), id);
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            if (response.getCode() == 404) {
                return null;
            } else {
                throw new CloudException("Error in response getting instance from OpenStack. Error code: " + response.getCode());
            }
        }
        final JsonObject results = parseObject(response.getBody());
        return InstanceConverter.fromJson(results.getJsonObject("server"), configuration.getAddressProvider());
    }

    @Override
    public String ip(final String id) throws CloudException {
        final CloudInstance instance = instance(id);
        return instance.getAddress();
    }

    @Override
    public void rebootInstance(String id) throws CloudException {
        final JsonObjectBuilder type = createObjectBuilder().add("type", "HARD");
        final String json = createObjectBuilder().add("reboot", type).build().toString();
        final String url = format("%s/v2/servers/%s/action", configuration.getComputeEndpoint(), id);
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, POST, headers, json);
        if (!response.isSuccessful()) {
            throw new CloudException(format("Could not reboot server with id %s and response %s: ", id, response.getBody()));
        }
    }

    @Override
    public void startInstance(String id) throws CloudException {
        final String json = createObjectBuilder().addNull("os-start").build().toString();
        final String url = format("%s/v2/servers/%s/action", configuration.getComputeEndpoint(), id);
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, POST, headers, json);
        if (!response.isSuccessful()) {
            throw new CloudException(format("Could not start server with id %s and response: %s ", id, response.getBody()));
        }
    }

    @Override
    public void shutdownInstance(String id) throws CloudException {
        final String json = createObjectBuilder().addNull("os-stop").build().toString();
        final String url = format("%s/v2/servers/%s/action", configuration.getComputeEndpoint(), id);
        final String authToken = authenticate();
        final Map<String, String> headers = buildDefaultHeaders(authToken);
        final HttpResponse response = httpClient.sendRequest(url, POST, headers, json);
        if (!response.isSuccessful()) {
            throw new CloudException(format("Could not shutdown server with id %s and response %s: ", id, response.getBody()));
        }
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
            .sorted(String::compareToIgnoreCase)
            .collect(toUnmodifiableList());
    }

}
