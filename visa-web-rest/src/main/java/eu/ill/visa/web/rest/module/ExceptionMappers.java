package eu.ill.visa.web.rest.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionMappers {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionMappers.class);

    @ServerExceptionMapper
    public RestResponse<String> mapException(JsonProcessingException exception) {
        logger.error("Error during JsonProcessing: {}", exception.getMessage());
        return RestResponse.status(Response.Status.INTERNAL_SERVER_ERROR, "JsonProcessingException: " + exception.getMessage());
    }
}
