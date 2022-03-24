package eu.ill.visa.cloud.http;


import java.util.Map;

public class HttpResponse {

    private final String              body;
    private final Integer             code;
    private final Map<String, String> headers;

    public HttpResponse(final String body, final Integer code, final Map<String, String> headers) {
        this.body = body;
        this.code = code;
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public Integer getCode() {
        return code;
    }

    public Boolean isCode(final int code) {
        return this.code == code;
    }

    public String getHeader(final String header) {
        if (headers.containsKey(header)) {
            return headers.get(header);
        }
        return null;
    }

    public String getHeaderIgnoreCase(final String header) {
        for (String aHeader : headers.keySet()) {
            if (aHeader.equalsIgnoreCase(header)) {
                return headers.get(aHeader);
            }
        }
        return null;
    }

    public boolean isSuccessful() {
        return this.code >= 200 && this.code < 300;
    }
}
