package de.htwg_in_schneider.checkmate.checkmate_backend.model;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

    

    @Entity
    public class AvailabilityRule {
    
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)

        private Long id;
    
        private Long tutorId;
    
        private DayOfWeek dayOfWeek;
    
        private LocalTime startTime;
        private LocalTime endTime;
    
        private Integer slotMinutes;
    

    // getters/setters
    public Long getId() { return id; }
    public Long getTutorId() { return tutorId; }
    public void setTutorId(Long tutorId) { this.tutorId = tutorId; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getSlotMinutes() { return slotMinutes; }
    public void setSlotMinutes(Integer slotMinutes) { this.slotMinutes = slotMinutes; }

}