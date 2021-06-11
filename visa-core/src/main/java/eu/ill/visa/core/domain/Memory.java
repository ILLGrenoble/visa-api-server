package eu.ill.visa.core.domain;

public class Memory {

    private Long total;
    private Long max;
    private Long free;

    public Memory() {

    }

    public Memory(Long total, Long max, Long free) {

        this.total = total;
        this.max = max;
        this.free = free;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    public Long getFree() {
        return free;
    }

    public void setFree(Long free) {
        this.free = free;
    }

    public Long getUsed() {
        return max - free;
    }
}
