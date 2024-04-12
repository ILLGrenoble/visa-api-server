package eu.ill.visa.web.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

public class InstanceCreatorDto {

    @NotNull
    @Size(min = 1, max = 250)
    private String name;

    @Size(max = 2500)
    private String comments;

    @NotNull
    private Long planId;

    @NotNull
    private Integer screenWidth;

    @NotNull
    private Integer screenHeight;

    @NotNull
    @AssertTrue
    private Boolean acceptedTerms;

    private Set<String> experiments;

    @NotNull
    private String keyboardLayout;

    public InstanceCreatorDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(Integer screenWidth) {
        this.screenWidth = screenWidth;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(Integer screenHeight) {
        this.screenHeight = screenHeight;
    }

    public Set<String> getExperiments() {
        return experiments;
    }

    public void setExperiments(Set<String> experiments) {
        this.experiments = experiments;
    }

    public Boolean getAcceptedTerms() {
        return acceptedTerms;
    }

    public void setAcceptedTerms(Boolean acceptedTerms) {
        this.acceptedTerms = acceptedTerms;
    }

    public String getKeyboardLayout() {
        return keyboardLayout;
    }

    public void setKeyboardLayout(String keyboardLayout) {
        this.keyboardLayout = keyboardLayout;
    }
}
