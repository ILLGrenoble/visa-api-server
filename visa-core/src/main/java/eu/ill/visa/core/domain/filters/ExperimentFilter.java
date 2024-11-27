package eu.ill.visa.core.domain.filters;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.QueryParam;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class ExperimentFilter {

    @QueryParam("userId")
    private String userId;

    @QueryParam("startDate")
    private DateParameter startDate;

    @QueryParam("endDate")
    private DateParameter endDate;

    @QueryParam("instrumentId")
    private Long instrumentId;

    @QueryParam("proposal")
    private Set<String> proposals;

    @QueryParam("proposalLike")
    private String proposalLike;

    @QueryParam("doi")
    private Set<String> dois;

    @QueryParam("includeOpenData")
    private Boolean includeOpenData = Boolean.FALSE;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public DateParameter getStartDate() {
        return startDate;
    }

    public DateParameter getEndDate() {
        return endDate;
    }

    public void setStartDate(DateParameter startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(DateParameter endDate) {
        this.endDate = endDate;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Set<String> getProposals() {
        return proposals;
    }

    public void setProposals(Set<String> proposals) {
        this.proposals = proposals;
    }

    public String getProposalLike() {
        return proposalLike;
    }

    public void setProposalLike(String proposalLike) {
        this.proposalLike = proposalLike;
    }

    public Set<String> getDois() {
        return dois;
    }

    public void setDois(Set<String> dois) {
        this.dois = dois;
    }

    public Boolean getIncludeOpenData() {
        return includeOpenData;
    }

    public void setIncludeOpenData(Boolean includeOpenData) {
        this.includeOpenData = includeOpenData;
    }

    public final static class DateParameter implements Serializable {
        private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        private Date date;

        public DateParameter() {
        }

        public DateParameter(Date date) {
            this.date = date;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public static DateParameter valueOf(String dateString) {
            try {
                final Date date = simpleDateFormat.parse(dateString);
                return new DateParameter(date);

            } catch(Exception e) {
                throw new BadRequestException("Could not convert dates");
            }
        }
    }
}
