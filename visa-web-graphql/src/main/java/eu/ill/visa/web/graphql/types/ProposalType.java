package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.Proposal;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("Proposal")
public class ProposalType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String identifier;
    private final @NotNull String title;
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


