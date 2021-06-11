package eu.ill.visa.vdi.support;

import com.corundumstudio.socketio.HandshakeData;

public class HttpRequest {

    private final HandshakeData handshakeData;

    public HttpRequest(final HandshakeData handshakeData) {
        this.handshakeData = handshakeData;
    }

    public String getStringParameter(final String key) {
        return this.handshakeData.getSingleUrlParam(key);
    }

}
