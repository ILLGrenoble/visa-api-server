package eu.ill.visa.web.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;

import jakarta.json.JsonObjectBuilder;
import jakarta.json.spi.JsonProvider;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.text.SimpleDateFormat;
import java.util.List;

public abstract class AbstractController {

    protected Response createResponse(final Object entity) {
        return createResponse(entity, Status.OK, null, null);
    }

    protected Response createResponse() {
        return createResponse(null, Status.OK, null, null);
    }

    protected Response createResponse(final Object entity, final Status status) {
        return createResponse(entity, status, null, null);
    }

    protected Response createResponse(final Object entity, final Status status, final ImmutableMap metadata) {
        return createResponse(entity, status, metadata, null);
    }

    protected Response createResponse(final Object entity, final Status status, final ImmutableMap metadata, final List<String> errors) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"));
        final ObjectNode json = mapper.createObjectNode();
        if (metadata != null) {
            json.putPOJO("_metadata", metadata);
        }
        json.putPOJO("data", entity);
        if (errors != null) {
            final ArrayNode errorsNode = json.putArray("errors");
            errors.forEach(errorsNode::add);
        }
        try {
            String jsonString = mapper.writeValueAsString(json);
            final ResponseBuilder response = Response.status(status).entity(jsonString);
            return response.build();

        } catch (JsonProcessingException e) {
            final ResponseBuilder response = Response.status(500).entity(e.getMessage());
            return response.build();
        }
    }


    protected JsonObjectBuilder createObjectBuilder() {
        return JsonProvider.provider().createObjectBuilder();
    }

    protected ObjectNode createObjectNode() {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.createObjectNode();
    }

    protected void checkNotNull(Object object, String message) {
        if (object == null) {
            throw new BadRequestException(message);
        }
    }


}
