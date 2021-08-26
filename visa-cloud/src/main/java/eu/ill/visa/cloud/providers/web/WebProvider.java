package eu.ill.visa.cloud.providers.web;

import eu.ill.visa.cloud.domain.*;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.http.HttpClient;
import eu.ill.visa.cloud.http.HttpResponse;
import eu.ill.visa.cloud.providers.CloudProvider;
import eu.ill.visa.cloud.providers.web.converters.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static eu.ill.visa.cloud.helpers.JsonHelper.*;
import static eu.ill.visa.cloud.http.HttpMethod.*;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableList;
import static javax.json.Json.createObjectBuilder;

/**
 * This provider enables VISA to another cloud provider i.e. proxmox, vmware etc.
 * It forwards requests to a web service implementation that encapsulates the underlying cloud provider
 */
public class WebProvider implements CloudProvider {

    private static final String HEADER_X_AUTH_TOKEN = "X-Auth-Token";

    private final        HttpClient               httpClient;
    private final        WebProviderConfiguration configuration;
    private static final Logger                   logger = LoggerFactory.getLogger(WebProvider.class);

    public WebProvider(final HttpClient httpClient,
                       final WebProviderConfiguration configuration) {
        this.httpClient = requireNonNull(httpClient, "httpClient cannot be null");
        this.configuration = requireNonNull(configuration, "configuration cannot be null");
    }

    @Override
    public List<CloudInstanceIdentifier> instanceIdentifiers() throws CloudException {
        final String url = format("%s/api/instances/identifiers", configuration.getUrl());
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (response.isSuccessful()) {
            final JsonObject results = parseObject(response.getBody());
            return buildInstanceIdentifiersResponse(results);
        }
        return null;
    }

    private List<CloudInstanceIdentifier> buildInstanceIdentifiersResponse(JsonObject response) {
        final List<CloudInstanceIdentifier> servers = new ArrayList<>();
        for (final JsonValue serverValue : response.getJsonArray("identifiers")) {
            final JsonObject cloudServerIdentifier = (JsonObject) serverValue;
            final CloudInstanceIdentifier server = InstanceIdentifierConverter.fromJson(cloudServerIdentifier);
            servers.add(server);
        }
        return servers;
    }

    private List<CloudInstance> buildInstancesResponse(final JsonArray response) {
        final List<CloudInstance> servers = new ArrayList<>();
        for (final JsonValue serverValue : response) {
            final JsonObject cloudServer = (JsonObject) serverValue;
            final CloudInstance server = buildInstanceResponse(cloudServer);
            servers.add(server);
        }
        return servers;
    }

    private CloudInstance buildInstanceResponse(final JsonValue serverValue) {
        final JsonObject cloudServer = (JsonObject) serverValue;
        return InstanceConverter.fromJson(cloudServer);
    }

    @Override
    public List<CloudInstance> instances() throws CloudException {
        final String url = format("%s/api/instances", configuration.getUrl());
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (response.isSuccessful()) {
            final JsonArray results = parseArray(response.getBody());
            return buildInstancesResponse(results);
        }
        return null;
    }

    @Override
    public CloudInstance instance(final String id) throws CloudException {
        final String url = format("%s/api/instances/%s", configuration.getUrl(), id);
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (response.isSuccessful()) {
            final JsonObject results = parseObject(response.getBody());
            return InstanceConverter.fromJson(results);
        }
        // not found...
        if (response.isCode(404)) {
            return null;
        }
        throw new CloudException(format("Error in response getting instance from provider. Error code: %s", response.getCode()));
    }

    @Override
    public void updateSecurityGroups(final String id, final List<String> securityGroupNames) throws CloudException {
        final String url = format("%s/api/instances/%s/security_groups", configuration.getUrl(), id);
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            logger.warn("Could not get security groups from the server with id {} and response {}", id, response.getBody());
        }
        final JsonArray currentSecurityGroups = parseArray(response.getBody());

        final List<String> currentSecurityGroupNames = IntStream.range(0, currentSecurityGroups.size()).mapToObj(currentSecurityGroups::getString).collect(toUnmodifiableList());
        final List<String> securityGroupNamesToRemove = currentSecurityGroupNames.stream().filter(name -> !securityGroupNames.contains(name)).collect(toUnmodifiableList());
        final List<String> securityGroupNamesToAdd = securityGroupNames.stream().filter(name -> !currentSecurityGroupNames.contains(name)).collect(toUnmodifiableList());

        logger.info("Updating instance {} security groups: removing [{}] and adding [{}]", id, String.join(", ", securityGroupNamesToRemove), String.join(", ", securityGroupNamesToAdd));

        // Remove obsolete security groups one by one
        for (String securityGroupName : securityGroupNamesToRemove) {
            final String json = createObjectBuilder().add("name", securityGroupName).build().toString();
            final String removeActionUrl = format("%s/api/instances/%s/security_groups/remove", configuration.getUrl(), id);
            final HttpResponse removeActionResponse = httpClient.sendRequest(removeActionUrl, POST, headers, json);
            if (!removeActionResponse.isSuccessful()) {
                logger.warn("Could not remove security group '{}' from the server id {} and response {}", securityGroupName, id, response.getBody());
            }
        }

        // Add missing security groups one by one
        for (String securityGroupName : securityGroupNamesToAdd) {
            final String json = createObjectBuilder().add("name", securityGroupName).build().toString();
            final String addActionUrl = format("%s/api/instances/%s/security_groups", configuration.getUrl(), id);
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
        final String url = format("%s/api/instances", configuration.getUrl());
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final JsonObjectBuilder builder = Json.createObjectBuilder();
        final String json = builder.add("name", name)
            .add("imageId", imageId)
            .add("flavourId", flavorId)
            .add("bootCommand", bootCommand)
            .add("securityGroups", toJsonArray(securityGroupNames))
            // remove all empty metadata values
            .add("metadata", toJsonObject(
                metadata.entrySet()
                    .stream()
                    .filter(entry -> !entry.getValue().isEmpty())
                    .collect(
                        toMap(Map.Entry::getKey,
                            Map.Entry::getValue
                        )
                    )
            ))
            .build()
            .toString();

        final HttpResponse response = httpClient.sendRequest(url, POST, headers, json);
        if (response.isSuccessful()) {
            final JsonObject result = parseObject(response.getBody());
            final String id = result.getString("id");
            return this.instance(id);
        }
        throw new CloudException(format("Could not create server with name %s and response %s ", name, response.getBody()));
    }

    @Override
    public List<CloudImage> images() throws CloudException {
        final String url = format("%s/api/images", configuration.getUrl());
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (response.isSuccessful()) {
            final JsonArray results = parseArray(response.getBody());
            final List<CloudImage> images = new ArrayList<>();
            for (final JsonValue imageValue : results) {
                final JsonObject cloudImage = (JsonObject) imageValue;
                final CloudImage image = ImageConverter.fromJson(cloudImage);
                images.add(image);
            }
            return images;
        }
        return new ArrayList<>();
    }

    @Override
    public CloudImage image(String id) throws CloudException {
        final String url = format("%s/api/images/%s", configuration.getUrl(), id);
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (response.isSuccessful()) {
            final JsonObject results = parseObject(response.getBody());
            return ImageConverter.fromJson(results);
        }
        return null;

    }

    @Override
    public List<CloudFlavour> flavors() throws CloudException {
        final String url = format("%s/api/flavours", configuration.getUrl());
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (response.isSuccessful()) {
            final JsonArray results = parseArray(response.getBody());
            final List<CloudFlavour> flavors = new ArrayList<>();
            for (final JsonValue flavorValue : results) {
                final JsonObject cloudFlavor = (JsonObject) flavorValue;
                final CloudFlavour flavor = FlavorConverter.fromJson(cloudFlavor);
                flavors.add(flavor);
            }
            return flavors;
        }
        return new ArrayList<>();
    }

    @Override
    public CloudFlavour flavor(String id) throws CloudException {
        final String url = format("%s/api/flavours/%s", configuration.getUrl(), id);
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            return null;
        }
        final JsonObject result = parseObject(response.getBody());
        return FlavorConverter.fromJson(result);
    }

    @Override
    public String ip(String id) throws CloudException {
        final CloudInstance instance = instance(id);
        return instance.getAddress();
    }

    @Override
    public void rebootInstance(String id) throws CloudException {
        final String url = format("%s/api/instances/%s/reboot", configuration.getUrl(), id);
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, POST, headers);
        if (!response.isSuccessful()) {
            throw new CloudException(format("Could not reboot server with id %s and response %s: ", id, response.getBody()));
        }
    }

    @Override
    public void shutdownInstance(String id) throws CloudException {
        final String url = format("%s/api/instances/%s/shutdown", configuration.getUrl(), id);
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, POST, headers);
        if (!response.isSuccessful()) {
            throw new CloudException(format("Could not shutdown server with id %s and response %s: ", id, response.getBody()));
        }
    }

    @Override
    public void startInstance(String id) throws CloudException {
        final String url = format("%s/api/instances/%s/start", configuration.getUrl(), id);
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, POST, headers);
        if (!response.isSuccessful()) {
            throw new CloudException(format("Could not start server with id %s and response %s: ", id, response.getBody()));
        }
    }

    @Override
    public void deleteInstance(String id) throws CloudException {
        final String url = format("%s/api/instances/%s", configuration.getUrl(), id);
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, DELETE, headers);
        if (!response.isSuccessful()) {
            throw new CloudException(format("Could not delete server with id %s and response %s", id, response.getBody()));
        }
    }

    @Override
    public CloudLimit limits() throws CloudException {
        final String url = format("%s/api/metrics", configuration.getUrl());
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            return null;
        }
        final JsonObject results = parseObject(response.getBody());
        return LimitConverter.fromJson(results);
    }

    @Override
    public List<String> securityGroups() throws CloudException {
        final String url = format("%s/api/security_groups", configuration.getUrl());
        final Map<String, String> headers = new HashMap<>() {{
            put(HEADER_X_AUTH_TOKEN, configuration.getAuthToken());
        }};
        final HttpResponse response = httpClient.sendRequest(url, GET, headers);
        if (!response.isSuccessful()) {
            logger.warn("Could not get security groups: {}", response.getBody());
        }
        final JsonArray currentSecurityGroups = parseArray(response.getBody());
        return IntStream.range(0, currentSecurityGroups.size())
            .mapToObj(currentSecurityGroups::getString)
            .collect(toUnmodifiableList());
    }
}
