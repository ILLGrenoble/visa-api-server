package eu.ill.visa.core.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

@Entity
@Table(name = "instrument_scientist")
public class InstrumentScientist {

    @EmbeddedId
    private InstrumentScientistKey id;

    @ManyToOne(optional = false)
    @MapsId("instrumentId")
    @JoinColumn(name = "instrument_id", foreignKey = @ForeignKey(name = "fk_instrument_id"), nullable = false)
    private Instrument instrument;

    @ManyToOne(optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_users_id"), nullable = false)
    private User user;

    public InstrumentScientist() {
    }

    public InstrumentScientistKey getId() {
        return id;
    }

    public void setId(InstrumentScientistKey id) {
        this.id = id;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Embeddable
    public static class InstrumentScientistKey implements Serializable {
        private Long instrumentId;
        private String userId;

        public InstrumentScientistKey() {
        }

        public InstrumentScientistKey(Long instrumentId, String userId) {
            this.instrumentId = instrumentId;
            this.userId = userId;
        }

        public Long getInstrumentId() {
            return instrumentId;
        }

        public void setInstrumentId(Long instrumentId) {
            this.instrumentId = instrumentId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            InstrumentScientistKey that = (InstrumentScientistKey) o;

            return new EqualsBuilder()
                .append(instrumentId, that.instrumentId)
                .append(userId, that.userId)
                .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                .append(instrumentId)
                .append(userId)
                .toHashCode();
        }
    }
}
