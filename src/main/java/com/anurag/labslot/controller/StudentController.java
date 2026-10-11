package com.anurag.labslot.controller;

import com.anurag.labslot.dto.Responses.UserView;
import com.anurag.labslot.service.SessionUsers;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students/me")
public class StudentController {
    private final SessionUsers sessions;
    public StudentController(SessionUsers sessions) { this.sessions = sessions; }
    @GetMapping public UserView me(HttpSession session) { return UserView.from(sessions.requireRole(session, "STUDENT")); }
}