package eu.ill.visa.web.dtos;

public class QuotaDto {

    private Integer maxInstances;
    private Long totalInstances;
    private Long availableInstances;

    public Integer getMaxInstances() {
        return maxInstances;
    }

    public void setMaxInstances(Integer maxInstances) {
        this.maxInstances = maxInstances;
    }

    public Long getTotalInstances() {
        return totalInstances;
    }

    public void setTotalInstances(Long totalInstances) {
        this.totalInstances = totalInstances;
    }

    public Long getAvailableInstances() {
        return availableInstances;
    }

    public void setAvailableInstances(Long availableInstances) {
        this.availableInstances = availableInstances;
    }


}
