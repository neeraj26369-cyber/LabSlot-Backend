package com.anurag.labslot.repository;

import com.anurag.labslot.model.Booking;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class BookingRepository {
    private static final org.springframework.jdbc.core.RowMapper<Booking> MAPPER = (rs, row) -> new Booking(
            rs.getLong("id"), rs.getLong("student_id"), rs.getString("student_name"), rs.getLong("equipment_id"),
            rs.getString("equipment_name"), rs.getDate("booking_date").toLocalDate(), rs.getTime("start_time").toLocalTime(),
            rs.getTime("end_time").toLocalTime(), rs.getString("purpose"), rs.getString("status"),
            rs.getTimestamp("created_at").toLocalDateTime());
    private static final String SELECT = "SELECT b.id,b.student_id,u.name AS student_name,b.equipment_id,e.name AS equipment_name,b.booking_date,b.start_time,b.end_time,b.purpose,b.status,b.created_at FROM bookings b JOIN users u ON u.id=b.student_id JOIN equipment e ON e.id=b.equipment_id";
    private final JdbcTemplate jdbc;
    public BookingRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Booking create(long studentId, long equipmentId, LocalDate date, LocalTime start, LocalTime end, String purpose) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO bookings(student_id,equipment_id,booking_date,start_time,end_time,purpose,status) VALUES(?,?,?,?,?,?, 'PENDING')", Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, studentId); statement.setLong(2, equipmentId); statement.setDate(3, java.sql.Date.valueOf(date));
            statement.setTime(4, java.sql.Time.valueOf(start)); statement.setTime(5, java.sql.Time.valueOf(end)); statement.setString(6, purpose);
            return statement;
        }, keys);
        Number id = keys.getKey();
        if (id == null) throw new IllegalStateException("The database did not return the new booking ID.");
        return findByIdForUpdate(id.longValue()).orElseThrow();
    }
    public Optional<Booking> findById(long id) { return jdbc.query(SELECT + " WHERE b.id=?", MAPPER, id).stream().findFirst(); }
    public Optional<Booking> findByIdForUpdate(long id) { return jdbc.query(SELECT + " WHERE b.id=? FOR UPDATE", MAPPER, id).stream().findFirst(); }
    public List<Booking> findByStudent(long studentId) { return jdbc.query(SELECT + " WHERE b.student_id=? ORDER BY b.created_at DESC", MAPPER, studentId); }
    public List<Booking> findAll(String status) {
        if (status == null) return jdbc.query(SELECT + " ORDER BY b.created_at DESC", MAPPER);
        return jdbc.query(SELECT + " WHERE b.status=? ORDER BY b.created_at DESC", MAPPER, status);
    }
    public List<Booking> findOverlapping(long equipmentId, LocalDate date, LocalTime start, LocalTime end, Long excludeId) {
        String sql = SELECT + " WHERE b.equipment_id=? AND b.booking_date=? AND b.status IN ('PENDING','APPROVED') AND b.start_time < ? AND b.end_time > ?";
        if (excludeId == null) return jdbc.query(sql, MAPPER, equipmentId, java.sql.Date.valueOf(date), java.sql.Time.valueOf(end), java.sql.Time.valueOf(start));
        return jdbc.query(sql + " AND b.id<>?", MAPPER, equipmentId, java.sql.Date.valueOf(date), java.sql.Time.valueOf(end), java.sql.Time.valueOf(start), excludeId);
    }
    public List<Booking> findOverlappingForUpdate(long equipmentId, LocalDate date, LocalTime start, LocalTime end, Long excludeId) {
        String sql = SELECT + " WHERE b.equipment_id=? AND b.booking_date=? AND b.status IN ('PENDING','APPROVED') AND b.start_time < ? AND b.end_time > ?";
        if (excludeId == null) return jdbc.query(sql + " FOR UPDATE", MAPPER, equipmentId, java.sql.Date.valueOf(date), java.sql.Time.valueOf(end), java.sql.Time.valueOf(start));
        return jdbc.query(sql + " AND b.id<>? FOR UPDATE", MAPPER, equipmentId, java.sql.Date.valueOf(date), java.sql.Time.valueOf(end), java.sql.Time.valueOf(start), excludeId);
    }
    public List<Booking> findActiveForEquipment(long equipmentId) {
        return jdbc.query(SELECT + " WHERE b.equipment_id=? AND b.status IN ('PENDING','APPROVED')", MAPPER, equipmentId);
    }
    public void updatePeriod(long id, LocalDate date, LocalTime start, LocalTime end, String purpose) {
        jdbc.update("UPDATE bookings SET booking_date=?,start_time=?,end_time=?,purpose=? WHERE id=?", java.sql.Date.valueOf(date), java.sql.Time.valueOf(start), java.sql.Time.valueOf(end), purpose, id);
    }
    public void updateStatus(long id, String status) { jdbc.update("UPDATE bookings SET status=? WHERE id=?", status, id); }
    public long countByStatus(String status) { return java.util.Objects.requireNonNull(jdbc.queryForObject("SELECT COUNT(*) FROM bookings WHERE status=?", Long.class, status)); }
}