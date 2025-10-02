package eu.ill.visa.core.domain.filters;


import eu.ill.visa.core.entity.enumerations.InstanceState;
import jakarta.ws.rs.QueryParam;

import java.util.List;

public class InstanceFilter {

    @QueryParam("id")
    private List<Long> ids;

    @QueryParam("nameLike")
    private String nameLike;

    @QueryParam("instrumentId")
    private Long instrumentId;

    @QueryParam("imageId")
    private Long imageId;

    @QueryParam("flavourId")
    private Long flavourId;

    @QueryParam("state")
    private InstanceState state;

    @QueryParam("ownerId")
    private String ownerId;

    @QueryParam("devicePoolId")
    private Long devicePoolId;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public String getNameLike() {
        return nameLike;
    }

    public void setNameLike(String nameLike) {
        this.nameLike = nameLike;
    }

    public Long getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Long instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public Long getFlavourId() {
        return flavourId;
    }

    public void setFlavourId(Long flavourId) {
        this.flavourId = flavourId;
    }

    public InstanceState getState() {
        return state;
    }

    public void setState(InstanceState state) {
        this.state = state;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Long getDevicePoolId() {
        return devicePoolId;
    }

    public void setDevicePoolId(Long devicePoolId) {
        this.devicePoolId = devicePoolId;
    }
}
