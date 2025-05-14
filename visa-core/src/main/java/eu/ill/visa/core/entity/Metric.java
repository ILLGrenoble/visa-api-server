package eu.ill.visa.core.entity;


import eu.ill.visa.core.entity.enumerations.MetricType;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "metric")
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "host", length = 250, nullable = false)
    private String host;

    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @Column(name = "attribute", length = 250, nullable = true)
    private String attribute;

    @Column(name = "statistic", nullable = false)
    private Double statistic;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private MetricType type;

    @Column(name = "record_count", nullable = false)
    private Long recordCount;

    @Column(name = "period_start", nullable = false)
    private Date periodStart;

    @Column(name = "period_end", nullable = false)
    private Date periodEnd;


    public Metric() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public Double getStatistic() {
        return statistic;
    }

    public void setStatistic(Double statistic) {
        this.statistic = statistic;
    }

    public MetricType getType() {
        return type;
    }

    public void setType(MetricType type) {
        this.type = type;
    }

    public Long getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Long recordCount) {
        this.recordCount = recordCount;
    }

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart) {
        this.periodStart = periodStart;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    public static final class Builder {
        private Long id;
        private String host;
        private String name;
        private String attribute;
        private Double statistic;
        private MetricType type;
        private Long recordCount;
        private Date periodStart;
        private Date periodEnd;

        private Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder attribute(String attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder statistic(Double statistic) {
            this.statistic = statistic;
            return this;
        }

        public Builder type(MetricType type) {
            this.type = type;
            return this;
        }

        public Builder recordCount(Long recordCount) {
            this.recordCount = recordCount;
            return this;
        }

        public Builder periodStart(Date periodStart) {
            this.periodStart = periodStart;
            return this;
        }

        public Builder periodEnd(Date periodEnd) {
            this.periodEnd = periodEnd;
            return this;
        }

        public Metric build() {
            Metric metric = new Metric();
            metric.setId(this.id);
            metric.setHost(this.host);
            metric.setName(this.name);
            metric.setAttribute(this.attribute);
            metric.setStatistic(this.statistic);
            metric.setType(this.type);
            metric.setRecordCount(this.recordCount);
            metric.setPeriodStart(this.periodStart);
            metric.setPeriodEnd(this.periodEnd);
            return metric;
        }
    }
}
