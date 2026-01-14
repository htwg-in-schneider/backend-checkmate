package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.AvailabilityRule;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Booking;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.AvailabilityRuleRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.BookingRepository;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/tutors")
public class AvailabilityController {

    private final BookingRepository bookingRepo;
    private final AvailabilityRuleRepository availabilityRuleRepo;

    public AvailabilityController(BookingRepository bookingRepo, AvailabilityRuleRepository availabilityRuleRepo) {
        this.bookingRepo = bookingRepo;
        this.availabilityRuleRepo = availabilityRuleRepo;
    }

    // ✅ 1) Dates: nur Tage zurückgeben, an denen es mind. 1 freien Slot gibt
    @GetMapping("/{tutorId}/available-dates")
    public List<String> getAvailableDates(
            @PathVariable Long tutorId,
            @RequestParam(defaultValue = "14") int days,
            @RequestParam(defaultValue = "60") int durationMinutes
    ) {
        LocalDate today = LocalDate.now();

        List<String> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = today.plusDays(i);
            if (hasFreeSlot(tutorId, d, durationMinutes)) {
                result.add(d.toString()); // "YYYY-MM-DD"
            }
        }
        return result;
    }

    private boolean hasFreeSlot(Long tutorId, LocalDate date, int durationMinutes) {
        DayOfWeek dow = date.getDayOfWeek();

        List<AvailabilityRule> rules = availabilityRuleRepo.findByTutorIdAndDayOfWeek(tutorId, dow);
        if (rules == null || rules.isEmpty()) return false;

        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

        List<Booking> bookings = bookingRepo.findByTutorIdAndStartAtLessThanAndStartAtGreaterThanEqual(
                tutorId, dayEnd, dayStart
        );

        int stepMinutes = 30;

        for (AvailabilityRule r : rules) {
            LocalTime t = r.getStartTime();
            while (!t.plusMinutes(durationMinutes).isAfter(r.getEndTime())) {
                LocalDateTime slotStart = LocalDateTime.of(date, t);
                LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);

                boolean overlaps = bookings.stream().anyMatch(b -> overlaps(slotStart, slotEnd, b));
                if (!overlaps) return true;

                t = t.plusMinutes(stepMinutes);
            }
        }

        return false;
    }

    // ✅ 2) Times: nur wirklich freie Zeiten (Rules minus Bookings)
    @GetMapping("/{tutorId}/available-times")
    public List<String> getAvailableTimes(
            @PathVariable Long tutorId,
            @RequestParam String date,                 // "YYYY-MM-DD"
            @RequestParam(defaultValue = "60") int durationMinutes
    ) {
        LocalDate day = LocalDate.parse(date);
        DayOfWeek dow = day.getDayOfWeek();

        // Verfügbarkeits-Regeln für diesen Wochentag
        List<AvailabilityRule> rules = availabilityRuleRepo.findByTutorIdAndDayOfWeek(tutorId, dow);
        if (rules == null || rules.isEmpty()) return List.of();

        LocalDateTime dayStart = day.atStartOfDay();
        LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();

        List<Booking> bookings = bookingRepo.findByTutorIdAndStartAtLessThanAndStartAtGreaterThanEqual(
                tutorId, dayEnd, dayStart
        );

        int stepMinutes = 30;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        List<String> result = new ArrayList<>();

        for (AvailabilityRule r : rules) {
            for (LocalTime t = r.getStartTime();
                 !t.plusMinutes(durationMinutes).isAfter(r.getEndTime());
                 t = t.plusMinutes(stepMinutes)) {

                LocalDateTime slotStart = LocalDateTime.of(day, t);
                LocalDateTime slotEnd = slotStart.plusMinutes(durationMinutes);

                boolean overlaps = bookings.stream().anyMatch(b -> overlaps(slotStart, slotEnd, b));
                if (!overlaps) result.add(t.format(fmt));
            }
        }

        return result.stream().distinct().sorted().toList();
    }

    private boolean overlaps(LocalDateTime slotStart, LocalDateTime slotEnd, Booking b) {
        LocalDateTime bStart = b.getStartAt();
        LocalDateTime bEnd = bStart.plusMinutes(b.getDurationMinutes());
        return slotStart.isBefore(bEnd) && bStart.isBefore(slotEnd);
    }
}