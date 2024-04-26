package eu.ill.visa.cloud.http.clients;

import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.http.HttpClient;
import eu.ill.visa.cloud.http.HttpMethod;
import eu.ill.visa.cloud.http.HttpResponse;
import okhttp3.*;
import okhttp3.internal.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class OkHttpClientAdapter implements HttpClient {

    private static final MediaType JSON_CONTENT_TYPE = MediaType.parse("application/json");

    private final OkHttpClient client;

    public OkHttpClientAdapter() {
        this.client = new OkHttpClient.Builder().callTimeout(60, TimeUnit.SECONDS).build();
    }

    private Headers buildHeaders(Map<String, String> headers) {
        Headers.Builder headersBuilder = new Headers.Builder();
        if (headers == null) {
            return headersBuilder.build();
        }
        final Iterator entries = headers.entrySet().iterator();
        while (entries.hasNext()) {
            Entry  header = (Entry) entries.next();
            String key    = (String) header.getKey();
            String value  = (String) header.getValue();
            headersBuilder.add(key, value);
        }
        return headersBuilder.build();
    }

    private HttpResponse buildResponse(final Response response) throws CloudException {
        try {
            final Map<String, String> retHeaders = new HashMap<>();
            final Headers             headers    = response.headers();

            for (final String name : headers.names()) {
                retHeaders.put(name, headers.get(name));
            }

            return new HttpResponse(response.body().string(),
                response.code(),
                retHeaders
            );
        } catch (IOException exception) {
            throw new CloudException("Error building response from HTTP request", exception);
        } finally {
            response.body().close();
        }
    }

    private HttpResponse doRequest(final Request request) throws CloudException {
        try {
            final Response response = client.newCall(request).execute();
            return buildResponse(response);
        } catch (IOException exception) {
            throw new CloudException("Error sending HTTP request", exception);
        }
    }

    private HttpResponse doPost(final String url, final Headers headers, final String data) throws CloudException {
        final RequestBody body = data == null ? Util.EMPTY_REQUEST : RequestBody.create(data, JSON_CONTENT_TYPE);
        final Request request = new Request.Builder()
            .url(url)
            .headers(headers)
            .post(body)
            .build();

        return doRequest(request);
    }

    private HttpResponse doGet(final String url, final Headers headers) throws CloudException {
        final Request request = new Request.Builder()
            .url(url)
            .headers(headers)
            .build();
        return doRequest(request);
    }

    private HttpResponse doDelete(final String url, final Headers headers) throws CloudException {
        final Request request = new Request.Builder()
            .url(url)
            .headers(headers)
            .delete()
            .build();
        return doRequest(request);
    }

    @Override
    public HttpResponse sendRequest(final String url,
                                    final HttpMethod method,
                                    final Map<String, String> headers) throws CloudException {
        return sendRequest(url, method, headers, null);
    }

    @Override
    public HttpResponse sendRequest(final String url,
                                    final HttpMethod method,
                                    final Map<String, String> headers,
                                    final String data) throws CloudException {
        switch (method) {
            case GET:
                return doGet(url, buildHeaders(headers));
            case POST:
                return doPost(url, buildHeaders(headers), data);
            case DELETE:
                return doDelete(url, buildHeaders(headers));
            default:
                throw new CloudException("HTTP method not supported: " + method);
        }
    }
}
