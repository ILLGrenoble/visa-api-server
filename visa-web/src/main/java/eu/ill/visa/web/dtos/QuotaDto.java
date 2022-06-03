package eu.ill.visa.web.dtos;

public class QuotaDto {

    private Integer creditsQuota;
    private Integer creditsUsed;
    private Integer creditsAvailable;

    public Integer getCreditsQuota() {
        return creditsQuota;
    }

    public void setCreditsQuota(Integer creditsQuota) {
        this.creditsQuota = creditsQuota;
    }

    public Integer getCreditsUsed() {
        return creditsUsed;
    }

    public void setCreditsUsed(Integer creditsUsed) {
        this.creditsUsed = creditsUsed;
    }

    public Integer getCreditsAvailable() {
        return this.creditsAvailable;
    }

    public void setCreditsAvailable(Integer creditsAvailable) {
        this.creditsAvailable = creditsAvailable;
    }
}
