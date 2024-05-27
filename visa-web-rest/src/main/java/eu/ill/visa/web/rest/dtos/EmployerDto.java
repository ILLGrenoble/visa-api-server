package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.Employer;

public class EmployerDto {
    private final Long id;
    private final String name;
    private final String town;
    private final String countryCode;

    public EmployerDto(final Employer employer) {
        this.id = employer.getId();
        this.name = employer.getName();
        this.town = employer.getTown();
        this.countryCode = employer.getCountryCode();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTown() {
        return town;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
