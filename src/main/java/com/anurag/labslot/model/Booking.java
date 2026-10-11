package com.anurag.labslot.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record Booking(long id, long studentId, String studentName, long equipmentId, String equipmentName,
                      LocalDate bookingDate, LocalTime startTime, LocalTime endTime, String purpose,
                      String status, LocalDateTime createdAt) {}