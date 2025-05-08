package mindgarden.db;

import mindgarden.model.Goal;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GoalDAO {

    public static List<Goal> getLatestGoalsByUser(int userId, int limit) {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT * FROM goals WHERE user_id = ? ORDER BY id DESC LIMIT ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Use a helper method to handle potential conversion issues
                LocalDate startDate = parseDate(rs, "start_date");
                LocalDate deadline = parseDate(rs, "deadline");

                Goal goal = new Goal(
                        rs.getString("title"),
                        rs.getString("type"),
                        rs.getString("description"),
                        startDate,
                        deadline,
                        rs.getBoolean("reminder"),
                        rs.getBoolean("track_progress"),
                        rs.getBoolean("notify_on_completion")
                );
                goal.setId(rs.getInt("id"));
                goal.setUserId(rs.getInt("user_id"));
                goals.add(goal);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return goals;
    }

    // Helper method to parse dates correctly
    private static LocalDate parseDate(ResultSet rs, String columnName) throws SQLException {
        try {
            Date sqlDate = rs.getDate(columnName); // Try to get date directly
            if (sqlDate != null) {
                return sqlDate.toLocalDate();
            }
        } catch (SQLException e) {
            // If direct parsing fails, try getting the value as a String
            String dateString = rs.getString(columnName);
            if (dateString != null) {
                return LocalDate.parse(dateString); // Parse from ISO-8601 string
            }
        }
        throw new SQLException("Unable to parse date for column: " + columnName);
    }


    public static void saveGoal(Goal goal) {
        String sql = """
            INSERT INTO goals (user_id, title, type, description, start_date, deadline, reminder, track_progress, notify_on_completion)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, goal.getUserId());
            stmt.setString(2, goal.getTitle());
            stmt.setString(3, goal.getType());
            stmt.setString(4, goal.getDescription());
            stmt.setString(5, goal.getStartDate().toString());     // Enregistre les dates comme texte
            stmt.setString(6, goal.getDeadline().toString());
            stmt.setBoolean(7, goal.isReminder());
            stmt.setBoolean(8, goal.isTrackProgress());
            stmt.setBoolean(9, goal.isNotifyOnCompletion());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateGoal(Goal goal) {
        String sql = """
            UPDATE goals SET title = ?, type = ?, description = ?, start_date = ?, deadline = ?,
            reminder = ?, track_progress = ?, notify_on_completion = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, goal.getTitle());
            stmt.setString(2, goal.getType());
            stmt.setString(3, goal.getDescription());
            stmt.setString(4, goal.getStartDate().toString());
            stmt.setString(5, goal.getDeadline().toString());
            stmt.setBoolean(6, goal.isReminder());
            stmt.setBoolean(7, goal.isTrackProgress());
            stmt.setBoolean(8, goal.isNotifyOnCompletion());
            stmt.setInt(9, goal.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteGoal(int id) {
        String sql = "DELETE FROM goals WHERE id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Goal> getGoalsByUser(int userId) {
        List<Goal> goals = new ArrayList<>();
        String sql = "SELECT * FROM goals WHERE user_id = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Goal goal = new Goal(
                        rs.getString("title"),
                        rs.getString("type"),
                        rs.getString("description"),
                        LocalDate.parse(rs.getString("start_date")),   // ⚠️ Convertir depuis String
                        LocalDate.parse(rs.getString("deadline")),
                        rs.getBoolean("reminder"),
                        rs.getBoolean("track_progress"),
                        rs.getBoolean("notify_on_completion")
                );
                goal.setId(rs.getInt("id"));
                goal.setUserId(rs.getInt("user_id"));
                goals.add(goal);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return goals;
    }
}
