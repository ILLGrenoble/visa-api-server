package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.Proposal;

public class ProposalDto {
    private final Long id;
    private final String identifier;
    private final String title;
    private final String url;
    private final String doi;

    public ProposalDto(final Proposal proposal) {
        this.id = proposal.getId();
        this.identifier = proposal.getIdentifier();
        this.title = proposal.getTitle();
        this.url = proposal.getUrl();
        this.doi = proposal.getDoi();
    }

    public Long getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getDoi() {
        return doi;
    }
}


