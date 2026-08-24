package eu.ill.visa.vdi.domain.models;


public record PingResponseData(PingSource source, long rttMs) {
    public enum PingSource {
        SERVER,
        CLIENT,
    }
}
