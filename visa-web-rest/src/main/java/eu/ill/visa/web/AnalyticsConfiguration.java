package eu.ill.visa.web;

import jakarta.validation.constraints.NotNull;

public class AnalyticsConfiguration {

    @NotNull
    private Boolean enabled;

    private String url;

    private Integer siteId;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }
}
