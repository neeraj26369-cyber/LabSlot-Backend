package com.anurag.labslot.controller;

import com.anurag.labslot.dto.Requests.BookingEdit;
import com.anurag.labslot.dto.Requests.BookingForm;
import com.anurag.labslot.dto.Responses.BookingView;
import com.anurag.labslot.model.User;
import com.anurag.labslot.service.BookingService;
import com.anurag.labslot.service.SessionUsers;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookings;
    private final SessionUsers sessions;
    public BookingController(BookingService bookings, SessionUsers sessions) { this.bookings = bookings; this.sessions = sessions; }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public BookingView create(@Valid @RequestBody BookingForm form, HttpSession session) {
        User student = sessions.requireRole(session, "STUDENT");
        return BookingView.from(bookings.create(student, form));
    }
    @GetMapping("/my") public List<BookingView> mine(HttpSession session) {
        User student = sessions.requireRole(session, "STUDENT");
        return bookings.mine(student.id()).stream().map(BookingView::from).toList();
    }
    @GetMapping public List<BookingView> all(@RequestParam(required = false) String status, HttpSession session) {
        sessions.requireRole(session, "ADMIN");
        return bookings.all(status).stream().map(BookingView::from).toList();
    }
    @GetMapping("/{id}") public BookingView get(@PathVariable long id, HttpSession session) {
        return BookingView.from(bookings.get(id, sessions.require(session)));
    }
    @PutMapping("/{id}") public BookingView update(@PathVariable long id, @Valid @RequestBody BookingEdit form, HttpSession session) {
        return BookingView.from(bookings.update(id, sessions.requireRole(session, "STUDENT"), form));
    }
    @PutMapping("/{id}/cancel") public BookingView cancel(@PathVariable long id, HttpSession session) {
        return BookingView.from(bookings.cancel(id, sessions.requireRole(session, "STUDENT")));
    }
    @PutMapping("/{id}/approve") public BookingView approve(@PathVariable long id, HttpSession session) {
        sessions.requireRole(session, "ADMIN");
        return BookingView.from(bookings.approve(id));
    }
    @PutMapping("/{id}/reject") public BookingView reject(@PathVariable long id, HttpSession session) {
        sessions.requireRole(session, "ADMIN");
        return BookingView.from(bookings.reject(id));
    }
}