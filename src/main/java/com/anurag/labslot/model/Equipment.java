package com.anurag.labslot.model;

public record Equipment(long id, String name, String description, String category, int quantity,
                        String location, String availabilityStatus) {}