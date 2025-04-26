package mindgarden.db;

import mindgarden.model.JournalEntry;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for JournalEntry objects.
 * Handles interactions with the JournalEntries table in the database.
 */
public class JournalEntryDAO {

    // Reusing the same formatter as MoodEntryDAO
    private static final DateTimeFormatter SQLITE_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Adds a new journal entry to the database.
     * The entry's timestamp is set by the database default (CURRENT_TIMESTAMP).
     *
     * @param content The text content of the journal entry.
     * @return true if the insertion was successful, false otherwise.
     */
    public boolean addJournalEntry(String content) {
        String sql = "INSERT INTO JournalEntries(entry_content) VALUES(?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, content);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding journal entry: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeQuietly(pstmt);
            DatabaseManager.closeQuietly(conn);
        }
    }

    /**
     * Retrieves all journal entries from the database, ordered by timestamp descending.
     *
     * @return A list of JournalEntry objects. Returns an empty list if none are found or an error occurs.
     */
    public List<JournalEntry> getAllJournalEntries() {
        String sql = "SELECT entry_id, entry_content, entry_timestamp FROM JournalEntries ORDER BY entry_timestamp DESC";
        List<JournalEntry> entries = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.connect();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                int id = rs.getInt("entry_id");
                String content = rs.getString("entry_content");
                String timestampStr = rs.getString("entry_timestamp");
                LocalDateTime timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"), SQLITE_DATETIME_FORMATTER);

                entries.add(new JournalEntry(id, content, timestamp));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving journal entries: " + e.getMessage());
        } catch (Exception e) {
             System.err.println("Error parsing timestamp or creating JournalEntry: " + e.getMessage());
        } finally {
            DatabaseManager.closeQuietly(rs);
            DatabaseManager.closeQuietly(stmt);
            DatabaseManager.closeQuietly(conn);
        }
        return entries;
    }

    // --- Potential future methods ---
    // public JournalEntry getJournalEntryById(int id) { ... }
    // public boolean deleteJournalEntry(int id) { ... }
    // public boolean updateJournalEntry(JournalEntry entry) { ... }

}
