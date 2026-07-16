package eu.ill.visa.vdi.domain.models;


public interface PingResponseHandler {
    void onPingResponse(PingResponseData pingResponseData);
}
