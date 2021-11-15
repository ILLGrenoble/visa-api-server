package eu.ill.visa.business.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.inject.Singleton;
import eu.ill.visa.business.SecurityGroupServiceClientConfiguration;
import eu.ill.visa.business.services.SecurityGroupService;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.SecurityGroup;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class SecurityGroupServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(SecurityGroupService.class);

    private static final MediaType JSON_CONTENT_TYPE = MediaType.parse("application/json");

    private final SecurityGroupServiceClientConfiguration configuration;
    private final OkHttpClient securityGroupServiceClient;
    private final ObjectMapper objectMapper;
    private final ObjectWriter objectWriter;

    public SecurityGroupServiceClient(final SecurityGroupServiceClientConfiguration configuration) {
        this.configuration = configuration;
        this.securityGroupServiceClient = new OkHttpClient.Builder().callTimeout(60, TimeUnit.SECONDS).build();
        this.objectMapper = new ObjectMapper();
        this.objectWriter = this.objectMapper.writer().withDefaultPrettyPrinter();
    }

    public List<SecurityGroup> getSecurityGroups(final Instance instance) {
        List<SecurityGroup> securityGroups = new ArrayList<>();

        // Verify that the application is configured to use the security group service
        if (this.configuration == null || !this.configuration.isEnabled() || this.configuration.getUrl() == null || this.configuration.getUrl().equals("")) {
            return securityGroups;
        }

        try {
            // Convert instance to json string
            String json = this.objectWriter.writeValueAsString(instance);

            logger.info("Requesting security groups for instance {} from {}...", instance.getId(), this.configuration.getUrl());

            final RequestBody body = RequestBody.create(JSON_CONTENT_TYPE, json);
            final Request request = new Request.Builder()
                .url(this.configuration.getUrl())
                .addHeader("x-auth-token", this.configuration.getAuthToken())
                .post(body)
                .build();

            // Call web service
            final Response response = this.securityGroupServiceClient.newCall(request).execute();

            if (response.code() == 200 && response.body() != null) {
                String jsonString = response.body().string();
                response.body().close();

                securityGroups = this.objectMapper.readValue(jsonString,  new TypeReference<List<SecurityGroup>>(){});

                List<String> securityGroupNames = securityGroups.stream().map(SecurityGroup::getName).collect(Collectors.toUnmodifiableList());
                logger.info("... got security groups [{}] for instance {}", String.join(", ", securityGroupNames), instance.getId());

            } else {
                logger.warn("Caught HTTP error ({}) getting security groups for instance {}: {} ", response.code(), instance.getId(), (response.body() == null ? "(no message)" : response.body().string()));
            }

        } catch (IOException e) {
            logger.warn("Caught exception getting security groups for instance {}: {} ", instance.getId(), e.getMessage());
        }

        return securityGroups;
    }
}
