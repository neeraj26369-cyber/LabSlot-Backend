package com.anurag.labslot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

public final class Requests {
    private Requests() {}
    public record Register(@NotBlank @Size(max = 100) String name, @NotBlank @Email @Size(max = 190) String email,
                           @NotBlank @Size(min = 8, max = 72) String password) {}
    public record Login(@NotBlank @Email String email, @NotBlank String password) {}
    public record EquipmentForm(@NotBlank @Size(max = 120) String name, @NotBlank @Size(max = 2000) String description,
                                @NotBlank @Size(max = 80) String category, @Min(1) @Max(10000) int quantity,
                                @NotBlank @Size(max = 120) String location, @NotBlank String availabilityStatus) {}
    public record BookingForm(@NotNull Long equipmentId, @NotNull @FutureOrPresent LocalDate bookingDate,
                              @NotNull LocalTime startTime, @NotNull LocalTime endTime,
                              @NotBlank @Size(max = 500) String purpose) {}
    public record BookingEdit(@NotNull @FutureOrPresent LocalDate bookingDate, @NotNull LocalTime startTime,
                              @NotNull LocalTime endTime, @NotBlank @Size(max = 500) String purpose) {}
}