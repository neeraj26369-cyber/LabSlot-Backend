package com.anurag.labslot.dto;

import com.anurag.labslot.model.Booking;
import com.anurag.labslot.model.Equipment;
import com.anurag.labslot.model.User;

public final class Responses {
    private Responses() {}
    public record UserView(long id, String name, String email, String role) {
        public static UserView from(User user) { return new UserView(user.id(), user.name(), user.email(), user.role()); }
    }
    public record EquipmentView(long id, String name, String description, String category, int quantity,
                                String location, String availabilityStatus) {
        public static EquipmentView from(Equipment item) { return new EquipmentView(item.id(), item.name(), item.description(), item.category(), item.quantity(), item.location(), item.availabilityStatus()); }
    }
    public record BookingView(long id, long studentId, String studentName, long equipmentId, String equipmentName,
                              java.time.LocalDate bookingDate, java.time.LocalTime startTime,
                              java.time.LocalTime endTime, String purpose, String status,
                              java.time.LocalDateTime createdAt) {
        public static BookingView from(Booking item) {
            return new BookingView(item.id(), item.studentId(), item.studentName(), item.equipmentId(), item.equipmentName(), item.bookingDate(), item.startTime(), item.endTime(), item.purpose(), item.status(), item.createdAt());
        }
    }
    public record Message(String message) {}
    public record Availability(long equipmentId, java.time.LocalDate bookingDate, java.time.LocalTime startTime,
                               java.time.LocalTime endTime, int quantity, int available) {}
    public record ApiError(String message, java.util.Map<String, String> fieldErrors) {}
}