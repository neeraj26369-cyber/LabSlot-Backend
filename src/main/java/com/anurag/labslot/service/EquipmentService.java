package com.anurag.labslot.service;

import com.anurag.labslot.dto.Requests.EquipmentForm;
import com.anurag.labslot.model.Booking;
import com.anurag.labslot.model.Equipment;
import com.anurag.labslot.repository.BookingRepository;
import com.anurag.labslot.repository.EquipmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EquipmentService {
    private static final List<String> STATUSES = List.of("AVAILABLE", "UNAVAILABLE", "MAINTENANCE");
    private final EquipmentRepository equipment;
    private final BookingRepository bookings;
    public EquipmentService(EquipmentRepository equipment, BookingRepository bookings) { this.equipment = equipment; this.bookings = bookings; }

    @Transactional(readOnly = true)
    public List<Equipment> search(String text, boolean administrator) { return equipment.search(text, administrator); }
    @Transactional(readOnly = true)
    public Equipment find(long id) { return equipment.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipment was not found.")); }
    @Transactional(readOnly = true)
    public long count() { return equipment.count(); }
    @Transactional(readOnly = true)
    public long countAvailable() { return equipment.countAvailable(); }
    @Transactional
    public Equipment create(EquipmentForm form) {
        validateStatus(form.availabilityStatus());
        return equipment.create(form.name().trim(), form.description().trim(), form.category().trim(), form.quantity(), form.location().trim(), form.availabilityStatus());
    }
    @Transactional
    public Equipment update(long id, EquipmentForm form) {
        validateStatus(form.availabilityStatus());
        Equipment old = equipment.lockById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipment was not found."));
        if (form.quantity() < old.quantity() && maximumConcurrent(bookings.findActiveForEquipment(id)) > form.quantity()) {
            throw new ApiException(HttpStatus.CONFLICT, "Quantity cannot be lower than units already booked at the same time.");
        }
        equipment.update(id, form.name().trim(), form.description().trim(), form.category().trim(), form.quantity(), form.location().trim(), form.availabilityStatus());
        return new Equipment(id, form.name().trim(), form.description().trim(), form.category().trim(), form.quantity(), form.location().trim(), form.availabilityStatus());
    }
    @Transactional
    public void delete(long id) {
        equipment.lockById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipment was not found."));
        if (equipment.hasBookings(id)) throw new ApiException(HttpStatus.CONFLICT, "Equipment with booking history cannot be deleted. Mark it unavailable instead.");
        equipment.delete(id);
    }
    private void validateStatus(String status) {
        if (status == null || !STATUSES.contains(status)) throw new ApiException(HttpStatus.BAD_REQUEST, "Availability status must be AVAILABLE, UNAVAILABLE, or MAINTENANCE.");
    }
    private int maximumConcurrent(List<Booking> rows) {
        int maximum = 0;
        for (Booking point : rows) {
            int count = (int) rows.stream().filter(row -> row.bookingDate().equals(point.bookingDate())
                    && !row.startTime().isAfter(point.startTime()) && row.endTime().isAfter(point.startTime())).count();
            maximum = Math.max(maximum, count);
        }
        return maximum;
    }
}