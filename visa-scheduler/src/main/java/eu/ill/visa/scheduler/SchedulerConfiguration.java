package eu.ill.visa.scheduler;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ill.visa.scheduler.tasks.TaskManagerConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SchedulerConfiguration {

    @NotNull
    private boolean enabled = true;

    @NotNull
    @Valid
    private TaskManagerConfiguration taskManagerConfiguration;

    @JsonProperty("taskManager")
    public TaskManagerConfiguration getTaskManagerConfiguration() {
        return taskManagerConfiguration;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
