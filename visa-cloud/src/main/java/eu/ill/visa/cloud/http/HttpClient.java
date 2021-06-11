package eu.ill.visa.cloud.http;


import eu.ill.visa.cloud.exceptions.CloudException;

import java.util.Map;

public interface HttpClient {
    HttpResponse sendRequest(final String url,
                             final HttpMethod method,
                             final Map<String, String> headers,
                             final String data) throws CloudException;

    HttpResponse sendRequest(final String url,
                             final HttpMethod method,
                             final Map<String, String> headers) throws CloudException;
}
