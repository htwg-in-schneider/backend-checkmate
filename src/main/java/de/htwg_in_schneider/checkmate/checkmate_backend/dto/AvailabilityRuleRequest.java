package de.htwg_in_schneider.checkmate.checkmate_backend.dto;

public class AvailabilityRuleRequest {
    public String dayOfWeek;    // "MONDAY"
    public String startTime;    // "09:00"
    public String endTime;      // "12:30"
    public Integer slotMinutes; // z.B. 30
}
