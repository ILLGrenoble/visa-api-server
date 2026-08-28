package eu.ill.visa.vdi.domain.models;


import java.util.Date;

public record PingResponseData(PingSource source, long rttMs, Date date) {
    public enum PingSource {
        SERVER,
        CLIENT,
    }
}
