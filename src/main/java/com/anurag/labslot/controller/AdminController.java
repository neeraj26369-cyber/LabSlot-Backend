package com.anurag.labslot.controller;

import com.anurag.labslot.service.BookingService;
import com.anurag.labslot.service.EquipmentService;
import com.anurag.labslot.service.SessionUsers;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final EquipmentService equipment;
    private final BookingService bookings;
    private final SessionUsers sessions;
    public AdminController(EquipmentService equipment, BookingService bookings, SessionUsers sessions) { this.equipment = equipment; this.bookings = bookings; this.sessions = sessions; }
    @GetMapping("/summary") public Map<String, Long> summary(HttpSession session) {
        sessions.requireRole(session, "ADMIN");
        return Map.of("equipmentCount", equipment.count(), "availableEquipmentCount", equipment.countAvailable(),
                "pendingBookings", bookings.count("PENDING"), "approvedBookings", bookings.count("APPROVED"),
                "rejectedBookings", bookings.count("REJECTED"), "cancelledBookings", bookings.count("CANCELLED"));
    }
}