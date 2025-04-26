package mindgarden.db;

import mindgarden.model.MoodEntry;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for MoodEntry objects.
 * Handles interactions with the MoodEntries table in the database.
 */
public class MoodEntryDAO {

    // Using ISO-8601 format which SQLite understands well for DATETIME
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Adds a new mood entry to the database.
     * The entry's timestamp is set by the database default (CURRENT_TIMESTAMP).
     *
     * @param moodType The type of mood recorded (e.g., "Happy").
     * @return true if the insertion was successful, false otherwise.
     */
    public boolean addMoodEntry(String moodType) {
        String sql = "INSERT INTO MoodEntries(mood_type) VALUES(?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, moodType);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding mood entry: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeQuietly(pstmt);
            DatabaseManager.closeQuietly(conn);
        }
    }

    /**
     * Retrieves all mood entries from the database, ordered by timestamp descending.
     *
     * @return A list of MoodEntry objects. Returns an empty list if none are found or an error occurs.
     */
    public List<MoodEntry> getAllMoodEntries() {
        String sql = "SELECT entry_id, mood_type, entry_timestamp FROM MoodEntries ORDER BY entry_timestamp DESC";
        List<MoodEntry> entries = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.connect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("entry_id");
                String moodType = rs.getString("mood_type");
                // SQLite stores DATETIME as TEXT in ISO format by default with CURRENT_TIMESTAMP
                String timestampStr = rs.getString("entry_timestamp");
                LocalDateTime timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"), SQLITE_DATETIME_FORMATTER); // Adjust format if needed

                entries.add(new MoodEntry(id, moodType, timestamp));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving mood entries: " + e.getMessage());
            // Return empty list on error
        } catch (Exception e) {
            System.err.println("Error parsing timestamp or creating MoodEntry: " + e.getMessage());
             // Return empty list on error
        }
        finally {
            DatabaseManager.closeQuietly(rs);
            DatabaseManager.closeQuietly(stmt);
            DatabaseManager.closeQuietly(conn);
        }
        return entries;
    }

    // --- Potential future methods ---
    // public List<MoodEntry> getMoodEntriesByDateRange(LocalDateTime start, LocalDateTime end) { ... }
    // public boolean deleteMoodEntry(int id) { ... }
    // public boolean updateMoodEntry(MoodEntry entry) { ... }

}
