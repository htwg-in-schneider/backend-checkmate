package de.htwg_in_schneider.checkmate.checkmate_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class TransactionCheckoutRequest {

    private String buyerEmail;
    private String note;

    @Valid
    @NotEmpty(message = "bookings must not be empty")
    private List<BookingRequest> bookings;

    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<BookingRequest> getBookings() { return bookings; }
    public void setBookings(List<BookingRequest> bookings) { this.bookings = bookings; }
}