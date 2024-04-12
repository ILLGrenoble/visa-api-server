package eu.ill.visa.web.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SMTPAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.CyclicBufferTracker;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonTypeName("email")
public class EmailAppenderFactory extends AbstractAppenderFactory<ILoggingEvent> {

    @NotNull
    private String host;

    @Min(1)
    @Max(65535)
    private int port;

    private boolean tls;

    private boolean ssl;

    private String username;

    private String password;

    private List<String> to;

    @NotNull
    private String from;

    private String subject;

    private boolean includeCallerData;

    private int bufferSize = 256;

    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public boolean isTls() {
        return tls;
    }

    @JsonProperty
    public void setTls(boolean tls) {
        this.tls = tls;
    }

    @JsonProperty
    public boolean isSsl() {
        return ssl;
    }

    @JsonProperty
    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    @JsonProperty
    public String getUsername() {
        return username;
    }

    @JsonProperty
    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty
    public List<String> getTo() {
        return to;
    }

    @JsonProperty
    public void setTo(List<String> to) {
        this.to = to;
    }

    @JsonProperty
    public String getFrom() {
        return from;
    }

    @JsonProperty
    public void setFrom(String from) {
        this.from = from;
    }

    @JsonProperty
    public String getSubject() {
        return subject;
    }

    @JsonProperty
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @JsonProperty
    public boolean isIncludeCallerData() {
        return includeCallerData;
    }

    @JsonProperty
    public void setIncludeCallerData(boolean includeCallerData) {
        this.includeCallerData = includeCallerData;
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String applicationName,
                                         LayoutFactory<ILoggingEvent> layoutFactory, LevelFilterFactory<ILoggingEvent> levelFilterFactory,
                                         AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory) {
        final SMTPAppender appender = new SMTPAppender();

        appender.setLayout(buildLayout(context, layoutFactory));
        appender.setContext(context);
        appender.setSMTPHost(host);
        appender.setSMTPPort(port);
        appender.setUsername(username);
        appender.setPassword(password);
        appender.setFrom(from);
        appender.setIncludeCallerData(includeCallerData);
        CyclicBufferTracker<ILoggingEvent> cbTracker = new CyclicBufferTracker<>();
        cbTracker.setBufferSize(bufferSize);
        appender.setCyclicBufferTracker(cbTracker);
        to.forEach(appender::addTo);

        // it only makes sense to set SSL or TLS, but not both
        // favor SSL over TLS since its preferable to start the connect
        // secure instead of having to switch during the process

        if (ssl) {
            appender.setSSL(true);
        } else if (tls) {
            appender.setSTARTTLS(true);
        }

        if (subject != null) {
            appender.setSubject(subject);
        }

        appender.start();

        return appender;
    }

}
