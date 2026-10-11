package com.anurag.labslot.repository;

import com.anurag.labslot.model.Equipment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class EquipmentRepository {
    private static final org.springframework.jdbc.core.RowMapper<Equipment> MAPPER = (rs, row) -> new Equipment(
            rs.getLong("id"), rs.getString("name"), rs.getString("description"), rs.getString("category"),
            rs.getInt("quantity"), rs.getString("location"), rs.getString("availability_status"));
    private final JdbcTemplate jdbc;
    public EquipmentRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Equipment> search(String search, boolean includeUnavailable) {
        String availability = includeUnavailable ? "" : " AND availability_status='AVAILABLE'";
        String sql = "SELECT id,name,description,category,quantity,location,availability_status FROM equipment WHERE 1=1" + availability;
        String term = search == null || search.isBlank() ? null : "%" + search.trim().toLowerCase() + "%";
        if (term == null) return jdbc.query(sql + " ORDER BY name", MAPPER);
        return jdbc.query(sql + " AND (LOWER(name) LIKE ? OR LOWER(category) LIKE ?) ORDER BY name", MAPPER, term, term);
    }
    public Optional<Equipment> findById(long id) {
        return jdbc.query("SELECT id,name,description,category,quantity,location,availability_status FROM equipment WHERE id=?", MAPPER, id).stream().findFirst();
    }
    public Optional<Equipment> lockById(long id) {
        return jdbc.query("SELECT id,name,description,category,quantity,location,availability_status FROM equipment WHERE id=? FOR UPDATE", MAPPER, id).stream().findFirst();
    }
    public Equipment create(String name, String description, String category, int quantity, String location, String status) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO equipment(name,description,category,quantity,location,availability_status) VALUES(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, name); statement.setString(2, description); statement.setString(3, category);
            statement.setInt(4, quantity); statement.setString(5, location); statement.setString(6, status);
            return statement;
        }, keys);
        Number id = keys.getKey();
        if (id == null) throw new IllegalStateException("The database did not return the new equipment ID.");
        return new Equipment(id.longValue(), name, description, category, quantity, location, status);
    }
    public void update(long id, String name, String description, String category, int quantity, String location, String status) {
        jdbc.update("UPDATE equipment SET name=?,description=?,category=?,quantity=?,location=?,availability_status=? WHERE id=?", name, description, category, quantity, location, status, id);
    }
    public void delete(long id) { jdbc.update("DELETE FROM equipment WHERE id=?", id); }
    public boolean hasBookings(long id) { return java.util.Objects.requireNonNull(jdbc.queryForObject("SELECT COUNT(*) FROM bookings WHERE equipment_id=?", Long.class, id)) > 0; }
    public long count() { return java.util.Objects.requireNonNull(jdbc.queryForObject("SELECT COUNT(*) FROM equipment", Long.class)); }
    public long countAvailable() { return java.util.Objects.requireNonNull(jdbc.queryForObject("SELECT COUNT(*) FROM equipment WHERE availability_status='AVAILABLE'", Long.class)); }
}