package com.anurag.labslot.service;

import com.anurag.labslot.dto.Requests.Register;
import com.anurag.labslot.model.User;
import com.anurag.labslot.repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordService passwords;
    public AuthService(UserRepository users, PasswordService passwords) { this.users = users; this.passwords = passwords; }

    @Transactional
    public User register(Register request) {
        String email = request.email().trim().toLowerCase();
        if (users.findByEmail(email).isPresent()) throw new ApiException(HttpStatus.CONFLICT, "That email is already registered.");
        try { return users.create(request.name().trim(), email, passwords.hash(request.password()), "STUDENT"); }
        catch (DuplicateKeyException exception) { throw new ApiException(HttpStatus.CONFLICT, "That email is already registered."); }
    }

    public User login(String email, String password) {
        User user = users.findByEmail(email.trim().toLowerCase()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect."));
        if (!passwords.matches(password, user.passwordHash())) throw new ApiException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect.");
        return user;
    }

    @Transactional
    public User setupAdministrator(Register request) {
        String email = request.email().trim().toLowerCase();
        if (users.findByEmail(email).isPresent()) throw new ApiException(HttpStatus.CONFLICT, "That email already belongs to an account.");
        try { return users.create(request.name().trim(), email, passwords.hash(request.password()), "ADMIN"); }
        catch (DuplicateKeyException exception) { throw new ApiException(HttpStatus.CONFLICT, "That email already belongs to an account."); }
    }

    @Transactional(readOnly = true)
    public boolean administratorSignupAvailable() { return true; }
}