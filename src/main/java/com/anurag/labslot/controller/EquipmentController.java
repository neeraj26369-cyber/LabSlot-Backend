package com.anurag.labslot.controller;

import com.anurag.labslot.dto.Requests.EquipmentForm;
import com.anurag.labslot.dto.Responses.Availability;
import com.anurag.labslot.dto.Responses.EquipmentView;
import com.anurag.labslot.model.Equipment;
import com.anurag.labslot.model.User;
import com.anurag.labslot.service.BookingService;
import com.anurag.labslot.service.EquipmentService;
import com.anurag.labslot.service.SessionUsers;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {
    private final EquipmentService equipment;
    private final BookingService bookings;
    private final SessionUsers sessions;
    public EquipmentController(EquipmentService equipment, BookingService bookings, SessionUsers sessions) { this.equipment = equipment; this.bookings = bookings; this.sessions = sessions; }

    @GetMapping
    public List<EquipmentView> list(@RequestParam(required = false) String search, HttpSession session) {
        User user = sessions.require(session);
        return equipment.search(search, user.role().equals("ADMIN")).stream().map(EquipmentView::from).toList();
    }
    @GetMapping("/{id}") public EquipmentView get(@PathVariable long id, HttpSession session) {
        sessions.require(session);
        return EquipmentView.from(equipment.find(id));
    }
    @GetMapping("/{id}/availability")
    public Availability availability(@PathVariable long id, @RequestParam LocalDate bookingDate,
                                     @RequestParam LocalTime startTime, @RequestParam LocalTime endTime,
                                     HttpSession session) {
        sessions.require(session);
        Equipment item = equipment.find(id);
        return new Availability(id, bookingDate, startTime, endTime, item.quantity(), bookings.available(id, bookingDate, startTime, endTime));
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public EquipmentView create(@Valid @RequestBody EquipmentForm form, HttpSession session) {
        sessions.requireRole(session, "ADMIN");
        return EquipmentView.from(equipment.create(form));
    }
    @PutMapping("/{id}") public EquipmentView update(@PathVariable long id, @Valid @RequestBody EquipmentForm form, HttpSession session) {
        sessions.requireRole(session, "ADMIN");
        return EquipmentView.from(equipment.update(id, form));
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id, HttpSession session) {
        sessions.requireRole(session, "ADMIN");
        equipment.delete(id);
    }
}