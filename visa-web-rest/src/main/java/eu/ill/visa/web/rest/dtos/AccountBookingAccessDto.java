package eu.ill.visa.web.rest.dtos;


public class AccountBookingAccessDto {
    private final boolean owner;
    private final boolean organiser;

    public AccountBookingAccessDto(boolean owner, boolean organiser) {
        this.owner = owner;
        this.organiser = organiser;
    }

    public boolean isOwner() {
        return owner;
    }

    public boolean isOrganiser() {
        return organiser;
    }
}
