package com.anurag.labslot.controller;

import com.anurag.labslot.dto.Requests.Login;
import com.anurag.labslot.dto.Requests.Register;
import com.anurag.labslot.dto.Responses.UserView;
import com.anurag.labslot.model.User;
import com.anurag.labslot.service.AuthService;
import com.anurag.labslot.service.SessionUsers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.security.SecureRandom;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final AuthService auth;
    private final SessionUsers sessions;
    public AuthController(AuthService auth, SessionUsers sessions) { this.auth = auth; this.sessions = sessions; }

    @GetMapping("/csrf")
    public Map<String, String> csrf(HttpSession session) {
        String token = (String) session.getAttribute("csrfToken");
        if (token == null) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute("csrfToken", token);
        }
        return Map.of("token", token);
    }

    @GetMapping("/setup-status")
    public Map<String, Boolean> setupStatus(HttpServletRequest request) {
        return Map.of("available", isLoopback(request.getRemoteAddr()));
    }

    @PostMapping("/setup-admin") @ResponseStatus(HttpStatus.CREATED)
    public UserView setupAdministrator(@Valid @RequestBody Register request, HttpServletRequest servletRequest, HttpSession session) {
        if (!isLoopback(servletRequest.getRemoteAddr())) throw new com.anurag.labslot.service.ApiException(HttpStatus.FORBIDDEN, "Administrator account creation is only available from this computer.");
        User user = auth.setupAdministrator(request);
        String csrfToken = (String) session.getAttribute("csrfToken");
        servletRequest.changeSessionId();
        if (csrfToken != null) session.setAttribute("csrfToken", csrfToken);
        session.setAttribute("labslotUser", user);
        return UserView.from(user);
    }

    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public UserView register(@Valid @RequestBody Register request) { return UserView.from(auth.register(request)); }

    @PostMapping("/login")
    public UserView login(@Valid @RequestBody Login request, HttpServletRequest servletRequest, HttpSession session) {
        User user = auth.login(request.email(), request.password());
        String csrfToken = (String) session.getAttribute("csrfToken");
        servletRequest.changeSessionId();
        if (csrfToken != null) session.setAttribute("csrfToken", csrfToken);
        session.setAttribute("labslotUser", user);
        return UserView.from(user);
    }

    @GetMapping("/me")
    public UserView me(HttpSession session) { return UserView.from(sessions.require(session)); }

    @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) { session.invalidate(); }

    private boolean isLoopback(String remoteAddress) {
        try { return InetAddress.getByName(remoteAddress).isLoopbackAddress(); }
        catch (UnknownHostException exception) { return false; }
    }
}