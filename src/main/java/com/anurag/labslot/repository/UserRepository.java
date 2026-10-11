package com.anurag.labslot.repository;

import com.anurag.labslot.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;
    public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<User> findByEmail(String email) {
        return jdbc.query("SELECT id,name,email,password_hash,role FROM users WHERE email=?", (rs, row) ->
                new User(rs.getLong("id"), rs.getString("name"), rs.getString("email"), rs.getString("password_hash"), rs.getString("role")), email).stream().findFirst();
    }

    public boolean administratorExists() {
        return java.util.Objects.requireNonNull(jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE role='ADMIN'", Long.class)) > 0;
    }

    public User create(String name, String email, String passwordHash, String role) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users(name,email,password_hash,role) VALUES(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, role);
            return statement;
        }, keys);
        Number id = keys.getKey();
        if (id == null) throw new IllegalStateException("The database did not return the new user ID.");
        return new User(id.longValue(), name, email, passwordHash, role);
    }

}