package com.anurag.labslot.service;

import com.anurag.labslot.dto.Requests.BookingEdit;
import com.anurag.labslot.dto.Requests.BookingForm;
import com.anurag.labslot.model.Booking;
import com.anurag.labslot.model.Equipment;
import com.anurag.labslot.model.User;
import com.anurag.labslot.repository.BookingRepository;
import com.anurag.labslot.repository.EquipmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookings;
    private final EquipmentRepository equipment;
    public BookingService(BookingRepository bookings, EquipmentRepository equipment) { this.bookings = bookings; this.equipment = equipment; }

    @Transactional
    public Booking create(User student, BookingForm form) {
        validatePeriod(form.bookingDate(), form.startTime(), form.endTime());
        Equipment item = lockBookable(form.equipmentId());
        ensureCapacity(item, form.bookingDate(), form.startTime(), form.endTime(), null);
        return bookings.create(student.id(), item.id(), form.bookingDate(), form.startTime(), form.endTime(), form.purpose().trim());
    }
    @Transactional(readOnly = true)
    public List<Booking> mine(long studentId) { return bookings.findByStudent(studentId); }
    @Transactional(readOnly = true)
    public List<Booking> all(String status) {
        if (status != null && !List.of("PENDING", "APPROVED", "REJECTED", "CANCELLED").contains(status)) throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown booking status.");
        return bookings.findAll(status);
    }
    @Transactional(readOnly = true)
    public Booking get(long id, User viewer) {
        Booking row = find(id);
        if (viewer.role().equals("STUDENT") && row.studentId() != viewer.id()) throw new ApiException(HttpStatus.FORBIDDEN, "You cannot view another student's booking.");
        return row;
    }
    @Transactional
    public Booking update(long id, User student, BookingEdit form) {
        Booking old = find(id);
        verifyOwner(old, student);
        requirePending(old);
        validatePeriod(form.bookingDate(), form.startTime(), form.endTime());
        Equipment item = lockBookable(old.equipmentId());
        Booking current = findForUpdate(id);
        verifyOwner(current, student);
        requirePending(current);
        ensureCapacity(item, form.bookingDate(), form.startTime(), form.endTime(), id);
        bookings.updatePeriod(id, form.bookingDate(), form.startTime(), form.endTime(), form.purpose().trim());
        return findForUpdate(id);
    }
    @Transactional
    public Booking cancel(long id, User student) {
        Booking old = find(id);
        verifyOwner(old, student);
        equipment.lockById(old.equipmentId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipment was not found."));
        Booking current = findForUpdate(id);
        verifyOwner(current, student);
        if (!current.status().equals("PENDING") && !current.status().equals("APPROVED")) throw new ApiException(HttpStatus.CONFLICT, "Only pending or approved bookings can be cancelled.");
        if (hasStarted(current)) throw new ApiException(HttpStatus.CONFLICT, "A booking cannot be cancelled after its start time.");
        bookings.updateStatus(id, "CANCELLED");
        return findForUpdate(id);
    }
    @Transactional
    public Booking approve(long id) { return decide(id, true); }
    @Transactional
    public Booking reject(long id) { return decide(id, false); }
    @Transactional(readOnly = true)
    public int available(long equipmentId, LocalDate date, LocalTime start, LocalTime end) {
        validatePeriod(date, start, end);
        Equipment item = equipment.findById(equipmentId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipment was not found."));
        if (!item.availabilityStatus().equals("AVAILABLE")) return 0;
        return Math.max(0, item.quantity() - peak(bookings.findOverlapping(equipmentId, date, start, end, null), start, end));
    }
    @Transactional(readOnly = true)
    public long count(String status) { return bookings.countByStatus(status); }
    private Booking decide(long id, boolean approve) {
        Booking old = find(id);
        requirePending(old);
        Equipment item = equipment.lockById(old.equipmentId()).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipment was not found."));
        Booking current = findForUpdate(id);
        requirePending(current);
        if (approve) {
            if (hasStarted(current)) throw new ApiException(HttpStatus.CONFLICT, "A booking cannot be approved after its start time.");
            ensureAvailable(item);
            ensureCapacity(item, current.bookingDate(), current.startTime(), current.endTime(), id);
        }
        bookings.updateStatus(id, approve ? "APPROVED" : "REJECTED");
        return findForUpdate(id);
    }
    private Equipment lockBookable(long id) {
        Equipment item = equipment.lockById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipment was not found."));
        ensureAvailable(item);
        return item;
    }
    private void ensureAvailable(Equipment item) {
        if (!item.availabilityStatus().equals("AVAILABLE")) throw new ApiException(HttpStatus.CONFLICT, "This equipment is unavailable or under maintenance.");
    }
    private void ensureCapacity(Equipment item, LocalDate date, LocalTime start, LocalTime end, Long excludeId) {
        int reserved = peak(bookings.findOverlappingForUpdate(item.id(), date, start, end, excludeId), start, end);
        if (reserved >= item.quantity()) throw new ApiException(HttpStatus.CONFLICT, "No units are free for that date and time.");
    }
    private int peak(List<Booking> rows, LocalTime start, LocalTime end) {
        int maximum = 0;
        for (LocalTime point : rows.stream().map(Booking::startTime).filter(time -> !time.isBefore(start) && time.isBefore(end)).distinct().toList()) {
            int count = (int) rows.stream().filter(row -> !row.startTime().isAfter(point) && row.endTime().isAfter(point)).count();
            maximum = Math.max(maximum, count);
        }
        int atStart = (int) rows.stream().filter(row -> !row.startTime().isAfter(start) && row.endTime().isAfter(start)).count();
        return Math.max(maximum, atStart);
    }
    private Booking find(long id) { return bookings.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking was not found.")); }
    private Booking findForUpdate(long id) { return bookings.findByIdForUpdate(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking was not found.")); }
    private void verifyOwner(Booking booking, User user) {
        if (booking.studentId() != user.id()) throw new ApiException(HttpStatus.FORBIDDEN, "You cannot change another student's booking.");
    }
    private void requirePending(Booking booking) {
        if (!booking.status().equals("PENDING")) throw new ApiException(HttpStatus.CONFLICT, "Only a pending booking can be changed.");
    }
    private boolean hasStarted(Booking booking) {
        return booking.bookingDate().isBefore(LocalDate.now()) || (booking.bookingDate().equals(LocalDate.now()) && !booking.startTime().isAfter(LocalTime.now()));
    }
    private void validatePeriod(LocalDate date, LocalTime start, LocalTime end) {
        if (date == null || date.isBefore(LocalDate.now())) throw new ApiException(HttpStatus.BAD_REQUEST, "Booking date must be today or a future date.");
        if (start == null || end == null || !start.isBefore(end)) throw new ApiException(HttpStatus.BAD_REQUEST, "Start time must be earlier than end time.");
        if (date.equals(LocalDate.now()) && !start.isAfter(LocalTime.now())) throw new ApiException(HttpStatus.BAD_REQUEST, "Start time must be later than the current time.");
    }
}