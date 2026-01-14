package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.dto.AvailableSlot;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.AvailabilityRule;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Booking;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.AvailabilityRuleRepository;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.BookingRepository;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tutors")
public class AvailabilityController {

    private final AvailabilityRuleRepository ruleRepo;
    private final BookingRepository bookingRepo;

    public AvailabilityController(AvailabilityRuleRepository ruleRepo, BookingRepository bookingRepo) {
        this.ruleRepo = ruleRepo;
        this.bookingRepo = bookingRepo;
    }

    // ✅ Regeln setzen (fürs Projekt: ohne Security erstmal ok, später Admin/Tutor schützen)
    @PostMapping("/{tutorId}/availability-rules")
    public List<AvailabilityRule> setRules(@PathVariable Long tutorId, @RequestBody List<AvailabilityRule> rules) {
        ruleRepo.deleteByTutorId(tutorId);
        for (var r : rules) r.setTutorId(tutorId);
        return ruleRepo.saveAll(rules);
    }

    @GetMapping("/{tutorId}/availability-rules")
    public List<AvailabilityRule> getRules(@PathVariable Long tutorId) {
        return ruleRepo.findByTutorId(tutorId);
    }

    // ✅ Slots generieren (next 14 days default)
    @GetMapping("/{tutorId}/available-slots")
    public List<AvailableSlot> getAvailableSlots(
            @PathVariable Long tutorId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false, defaultValue = "14") int days
    ) {
        LocalDate start = (from != null) ? LocalDate.parse(from) : LocalDate.now();
        LocalDate end = start.plusDays(days);

        var rules = ruleRepo.findByTutorId(tutorId);

        LocalDateTime fromDt = start.atStartOfDay();
        LocalDateTime toDt = end.atTime(23, 59);

        // gebuchte Startzeiten (damit sie nicht mehr angezeigt werden)
        Set<LocalDateTime> bookedStarts = bookingRepo
                .findByTutorIdAndStartAtBetween(tutorId, fromDt, toDt)
                .stream()
                .map(Booking::getStartAt)
                .collect(Collectors.toSet());

        List<AvailableSlot> result = new ArrayList<>();

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();

            for (AvailabilityRule r : rules) {
                if (r.getDayOfWeek() != dow) continue;

                int slotMinutes = (r.getSlotMinutes() != null) ? r.getSlotMinutes() : 60;

                LocalTime t = r.getStartTime();
                while (!t.plusMinutes(slotMinutes).isAfter(r.getEndTime())) {
                    LocalDateTime slotStart = LocalDateTime.of(d, t);
                    if (!bookedStarts.contains(slotStart)) {
                        result.add(new AvailableSlot(slotStart, slotMinutes));
                    }
                    t = t.plusMinutes(slotMinutes);
                }
            }
        }

        result.sort(Comparator.comparing(s -> s.startAt));
        return result;
    }
}