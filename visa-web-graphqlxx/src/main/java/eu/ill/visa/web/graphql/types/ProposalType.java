package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.Proposal;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

public class ProposalType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final String identifier;
    private final String title;
    private final String summary;

    public ProposalType(final Proposal proposal) {
        this.id = proposal.getId();
        this.identifier = proposal.getIdentifier();
        this.title = proposal.getTitle();
        this.summary = proposal.getSummary();
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

    public String getSummary() {
        return summary;
    }
}


