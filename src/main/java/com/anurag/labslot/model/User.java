package com.anurag.labslot.model;

public record User(long id, String name, String email, String passwordHash, String role) {}