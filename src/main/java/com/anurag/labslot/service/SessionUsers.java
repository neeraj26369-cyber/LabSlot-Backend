package com.anurag.labslot.service;

import com.anurag.labslot.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SessionUsers {
    public User require(HttpSession session) {
        Object value = session.getAttribute("labslotUser");
        if (value instanceof User user) return user;
        throw new ApiException(HttpStatus.UNAUTHORIZED, "Sign in to continue.");
    }
    public User requireRole(HttpSession session, String role) {
        User user = require(session);
        if (user.role() == null || !role.equalsIgnoreCase(user.role())) throw new ApiException(HttpStatus.FORBIDDEN, "This action is not available for your account.");
        return user;
    }
}