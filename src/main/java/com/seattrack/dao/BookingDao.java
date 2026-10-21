package com.seattrack.dao;

import com.seattrack.config.Database;
import com.seattrack.model.RegistrationView;
import com.seattrack.model.WorkshopView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BookingDao {
    public enum BookingStatus {
        SUCCESS,
        SOLD_OUT,
        ALREADY_REGISTERED,
        NOT_FOUND
    }

    public record BookingResult(BookingStatus status, RegistrationView registration, long registrationCount) {
    }

    public record CancelResult(boolean cancelled, long workshopId, long registrationCount) {
    }

    public List<WorkshopView> findWorkshops(String department) throws SQLException {
        String sql = "SELECT WORKSHOP_ID, DEPARTMENT, TITLE, DESCRIPTION, TOTAL_SEATS, AVAILABLE_SEATS "
                + "FROM WORKSHOPS "
                + (department == null ? "" : "WHERE DEPARTMENT = ? ")
                + "ORDER BY DEPARTMENT, TITLE";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (department != null) {
                statement.setString(1, department);
            }
            try (ResultSet rows = statement.executeQuery()) {
                List<WorkshopView> workshops = new ArrayList<>();
                while (rows.next()) {
                    workshops.add(new WorkshopView(
                            rows.getLong("WORKSHOP_ID"),
                            rows.getString("DEPARTMENT"),
                            rows.getString("TITLE"),
                            rows.getString("DESCRIPTION"),
                            rows.getInt("TOTAL_SEATS"),
                            rows.getInt("AVAILABLE_SEATS")));
                }
                return workshops;
            }
        }
    }

    public List<String> findDepartments() throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT DISTINCT DEPARTMENT FROM WORKSHOPS ORDER BY DEPARTMENT");
             ResultSet rows = statement.executeQuery()) {
            List<String> departments = new ArrayList<>();
            while (rows.next()) {
                departments.add(rows.getString(1));
            }
            return departments;
        }
    }

    public long countRegistrations() throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) FROM REGISTRATIONS");
             ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getLong(1);
        }
    }

    public BookingResult register(long workshopId, String name, String email) throws SQLException {
        String lockSql = """
                SELECT WORKSHOP_ID, TITLE, AVAILABLE_SEATS
                FROM WORKSHOPS
                WHERE WORKSHOP_ID = ?
                FOR UPDATE
                """;
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long workshopIdFromDb;
                String title;
                int seats;
                try (PreparedStatement lock = connection.prepareStatement(lockSql)) {
                    lock.setLong(1, workshopId);
                    try (ResultSet row = lock.executeQuery()) {
                        if (!row.next()) {
                            connection.rollback();
                            return new BookingResult(BookingStatus.NOT_FOUND, null, 0);
                        }
                        workshopIdFromDb = row.getLong("WORKSHOP_ID");
                        title = row.getString("TITLE");
                        seats = row.getInt("AVAILABLE_SEATS");
                    }
                }

                try (PreparedStatement duplicate = connection.prepareStatement(
                        "SELECT 1 FROM REGISTRATIONS WHERE WORKSHOP_ID = ? AND LOWER(STUDENT_EMAIL) = LOWER(?)")) {
                    duplicate.setLong(1, workshopId);
                    duplicate.setString(2, email);
                    try (ResultSet row = duplicate.executeQuery()) {
                        if (row.next()) {
                            connection.rollback();
                            return new BookingResult(BookingStatus.ALREADY_REGISTERED, null, 0);
                        }
                    }
                }

                if (seats <= 0) {
                    connection.rollback();
                    return new BookingResult(BookingStatus.SOLD_OUT, null, 0);
                }

                Instant registeredAt = Instant.now();
                long registrationId;
                try (PreparedStatement update = connection.prepareStatement(
                        "UPDATE WORKSHOPS SET AVAILABLE_SEATS = AVAILABLE_SEATS - 1 WHERE WORKSHOP_ID = ? AND AVAILABLE_SEATS > 0")) {
                    update.setLong(1, workshopId);
                    if (update.executeUpdate() != 1) {
                        connection.rollback();
                        return new BookingResult(BookingStatus.SOLD_OUT, null, 0);
                    }
                }
                String insertSql = """
                        INSERT INTO REGISTRATIONS (STUDENT_NAME, STUDENT_EMAIL, WORKSHOP_ID, REGISTERED_AT)
                        VALUES (?, ?, ?, ?)
                        """;
                try (PreparedStatement insert = connection.prepareStatement(insertSql, new String[]{"REG_ID"})) {
                    insert.setString(1, name);
                    insert.setString(2, email);
                    insert.setLong(3, workshopId);
                    insert.setTimestamp(4, Timestamp.from(registeredAt));
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("MySQL did not return the new registration ID.");
                        }
                        registrationId = keys.getLong(1);
                    }
                }
                long count;
                try (PreparedStatement countQuery = connection.prepareStatement(
                        "SELECT COUNT(*) FROM REGISTRATIONS");
                     ResultSet row = countQuery.executeQuery()) {
                    row.next();
                    count = row.getLong(1);
                }
                connection.commit();
                RegistrationView registration = new RegistrationView(
                        registrationId, name, email, workshopIdFromDb, title, registeredAt);
                return new BookingResult(BookingStatus.SUCCESS, registration, count);
            } catch (SQLException | RuntimeException error) {
                connection.rollback();
                throw error;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public CancelResult cancel(long registrationId) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Long workshopId = null;
                try (PreparedStatement find = connection.prepareStatement(
                        "SELECT WORKSHOP_ID FROM REGISTRATIONS WHERE REG_ID = ? FOR UPDATE")) {
                    find.setLong(1, registrationId);
                    try (ResultSet row = find.executeQuery()) {
                        if (row.next()) {
                            workshopId = row.getLong(1);
                        }
                    }
                }
                if (workshopId == null) {
                    connection.rollback();
                    return new CancelResult(false, 0, 0);
                }
                try (PreparedStatement delete = connection.prepareStatement(
                        "DELETE FROM REGISTRATIONS WHERE REG_ID = ?")) {
                    delete.setLong(1, registrationId);
                    if (delete.executeUpdate() != 1) {
                        connection.rollback();
                        return new CancelResult(false, 0, 0);
                    }
                }
                try (PreparedStatement restore = connection.prepareStatement(
                        "UPDATE WORKSHOPS SET AVAILABLE_SEATS = AVAILABLE_SEATS + 1 WHERE WORKSHOP_ID = ? AND AVAILABLE_SEATS < TOTAL_SEATS")) {
                    restore.setLong(1, workshopId);
                    if (restore.executeUpdate() != 1) {
                        throw new SQLException("Could not restore the cancelled workshop seat.");
                    }
                }
                long count;
                try (PreparedStatement countQuery = connection.prepareStatement(
                        "SELECT COUNT(*) FROM REGISTRATIONS");
                     ResultSet row = countQuery.executeQuery()) {
                    row.next();
                    count = row.getLong(1);
                }
                connection.commit();
                return new CancelResult(true, workshopId, count);
            } catch (SQLException | RuntimeException error) {
                connection.rollback();
                throw error;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Object[]> readAdminTable(String tableName) throws SQLException {
        String sql = switch (tableName) {
            case "WORKSHOPS" -> "SELECT * FROM WORKSHOPS ORDER BY WORKSHOP_ID";
            case "REGISTRATIONS" -> """
                    SELECT REG_ID, STUDENT_NAME, STUDENT_EMAIL, WORKSHOP_ID, REGISTERED_AT
                    FROM REGISTRATIONS ORDER BY REG_ID
                    """;
            default -> throw new IllegalArgumentException("Unsupported admin table: " + tableName);
        };
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            int columnCount = rows.getMetaData().getColumnCount();
            List<Object[]> result = new ArrayList<>();
            while (rows.next()) {
                Object[] values = new Object[columnCount];
                for (int column = 1; column <= columnCount; column++) {
                    values[column - 1] = rows.getObject(column);
                }
                result.add(values);
            }
            return result;
        }
    }

    public List<String> readAdminColumns(String tableName) throws SQLException {
        String sql = switch (tableName) {
            case "WORKSHOPS" -> "SELECT * FROM WORKSHOPS WHERE 1 = 0";
            case "REGISTRATIONS" -> "SELECT * FROM REGISTRATIONS WHERE 1 = 0";
            default -> throw new IllegalArgumentException("Unsupported admin table: " + tableName);
        };
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(sql)) {
            List<String> columns = new ArrayList<>();
            for (int column = 1; column <= rows.getMetaData().getColumnCount(); column++) {
                columns.add(rows.getMetaData().getColumnLabel(column));
            }
            return columns;
        }
    }

}
