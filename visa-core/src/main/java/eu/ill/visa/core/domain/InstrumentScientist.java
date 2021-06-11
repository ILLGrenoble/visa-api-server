package eu.ill.visa.core.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class InstrumentScientist {

    private InstrumentScientistKey id;

    private Instrument instrument;
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
