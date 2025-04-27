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

    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Adds a new mood entry without notes.
     *
     * @param moodType The type of mood recorded.
     * @return true if the insertion was successful, false otherwise.
     */
    public boolean addMoodEntry(String moodType) {
        return addMoodEntryWithNotes(moodType, null);
    }

    /**
     * Adds a new mood entry with optional notes.
     *
     * @param moodType The mood type (e.g., Happy, Sad).
     * @param notes    Optional additional notes (can be null).
     * @return true if insertion successful, false otherwise.
     */
    public boolean addMoodEntryWithNotes(String moodType, String notes) {
        String sql = "INSERT INTO MoodEntries (mood_type, entry_timestamp, notes) VALUES (?, CURRENT_TIMESTAMP, ?)";
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, moodType);

            if (notes != null && !notes.trim().isEmpty()) {
                pstmt.setString(2, notes);
            } else {
                pstmt.setNull(2, Types.VARCHAR);
            }

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error adding mood entry with notes: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all mood entries ordered by timestamp descending.
     *
     * @return List of MoodEntry.
     */
    public List<MoodEntry> getAllMoodEntries() {
        List<MoodEntry> entries = new ArrayList<>();
        String sql = "SELECT entry_id, mood_type, entry_timestamp, notes FROM MoodEntries ORDER BY entry_timestamp DESC";

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("entry_id");
                String moodType = rs.getString("mood_type");
                String timestampStr = rs.getString("entry_timestamp");
                LocalDateTime timestamp = parseTimestamp(timestampStr);
                String notes = rs.getString("notes");

                MoodEntry entry = new MoodEntry(id, moodType, timestamp, notes);
                entries.add(entry);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving mood entries: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing timestamp or creating MoodEntry: " + e.getMessage());
        }

        return entries;
    }

    /**
     * Parses a timestamp string safely.
     *
     * @param timestampStr The timestamp string.
     * @return LocalDateTime parsed, or now() if parsing fails.
     */
    private LocalDateTime parseTimestamp(String timestampStr) {
        try {
            return LocalDateTime.parse(timestampStr.replace(" ", "T"), SQLITE_DATETIME_FORMATTER);
        } catch (Exception e) {
            System.err.println("Error parsing timestamp: " + e.getMessage());
            return LocalDateTime.now();
        }
    }
}
