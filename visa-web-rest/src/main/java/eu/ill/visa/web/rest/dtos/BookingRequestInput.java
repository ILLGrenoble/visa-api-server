package eu.ill.visa.web.rest.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingRequestInput {

    @NotNull
    private Date startDate;

    @NotNull
    private Date endDate;

    @NotEmpty
    private List<BookingRequestFlavourInput> flavourRequests = new ArrayList<>();

    @NotNull
    @Size(max = 2500)
    private String comments;

    public BookingRequestInput() {
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<BookingRequestFlavourInput> getFlavourRequests() {
        return flavourRequests;
    }

    public void setFlavourRequests(List<BookingRequestFlavourInput> flavourRequests) {
        this.flavourRequests = flavourRequests;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public static final class BookingRequestFlavourInput {
        @NotNull
        private Long flavourId;

        @NotNull
        @Min(1)
        private Long quantity;

        public Long getFlavourId() {
            return flavourId;
        }

        public void setFlavourId(Long flavourId) {
            this.flavourId = flavourId;
        }

        public Long getQuantity() {
            return quantity;
        }

        public void setQuantity(Long quantity) {
            this.quantity = quantity;
        }
    }
}
