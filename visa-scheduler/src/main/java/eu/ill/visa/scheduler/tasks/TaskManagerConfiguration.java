package eu.ill.visa.scheduler.tasks;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class TaskManagerConfiguration {

    @NotNull
    @Valid
    private Integer numberThreads = 5;

    public Integer getNumberThreads() {
        return numberThreads;
    }

    public void setNumberThreads(Integer numberThreads) {
        this.numberThreads = numberThreads;
    }
}
