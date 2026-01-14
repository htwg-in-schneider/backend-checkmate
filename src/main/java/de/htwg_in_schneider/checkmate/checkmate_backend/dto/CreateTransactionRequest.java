package de.htwg_in_schneider.checkmate.checkmate_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CreateTransactionRequest {

    @NotEmpty(message = "items darf nicht leer sein")
    @Valid
    private List<CreateTransactionItem> items;

    @Email(message = "buyerEmail muss eine gültige Email sein")
    private String buyerEmail;

    private String note;

    public List<CreateTransactionItem> getItems() { return items; }
    public void setItems(List<CreateTransactionItem> items) { this.items = items; }

    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}