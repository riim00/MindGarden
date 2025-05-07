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
    public boolean addJournalEntry(String content, String title, String mood) {
        String sql = "INSERT INTO JournalEntries(entry_content, is_draft, title, mood) VALUES(?, 0, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, content);
            pstmt.setString(2, title);
            pstmt.setString(3, mood);
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

    public boolean addJournalEntryAsDraft(String content, String title, String mood) {
        String sql = "INSERT INTO JournalEntries(entry_content, is_draft, title, mood) VALUES(?, 1, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, content);
            pstmt.setString(2, title);
            pstmt.setString(3, mood);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding draft journal entry: " + e.getMessage());
            return false;
        } finally {
            DatabaseManager.closeQuietly(pstmt);
            DatabaseManager.closeQuietly(conn);
        }
    }

    /**
     * Retrieves the most recent journal entries from the database.
     *
     * @param limit Number of entries to retrieve
     * @return A list of JournalEntry objects. Returns an empty list if none are found.
     */
    public List<JournalEntry> getRecentJournalEntries(int limit) {
        String sql = "SELECT entry_id, entry_content, entry_timestamp, is_draft, title, mood FROM JournalEntries " +
                "WHERE is_draft = 0 ORDER BY entry_timestamp DESC LIMIT ?";
        List<JournalEntry> entries = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("entry_id");
                String content = rs.getString("entry_content");
                String timestampStr = rs.getString("entry_timestamp");
                boolean isDraft = rs.getInt("is_draft") == 1;
                String title = rs.getString("title");
                String mood = rs.getString("mood");

                LocalDateTime timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"), SQLITE_DATETIME_FORMATTER);

                entries.add(new JournalEntry(id, content, timestamp, isDraft, title, mood));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving recent journal entries: " + e.getMessage());
        } finally {
            DatabaseManager.closeQuietly(rs);
            DatabaseManager.closeQuietly(pstmt);
            DatabaseManager.closeQuietly(conn);
        }
        return entries;
    }

    /**
     * Récupère les entrées de journal les plus récentes
     * @param limit Le nombre d'entrées à récupérer
     * @return Une liste des entrées de journal les plus récentes
     */
    public List<JournalEntry> getRecentEntries(int limit) {
        List<JournalEntry> entries = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.connect();
            String sql = "SELECT entry_id, entry_content, title, mood, entry_timestamp, is_draft " +
                    "FROM JournalEntries " +
                    "WHERE is_draft = 0 " +
                    "ORDER BY entry_timestamp DESC " +
                    "LIMIT ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("entry_id");
                String content = rs.getString("entry_content");
                String title = rs.getString("title");
                String mood = rs.getString("mood");
                LocalDateTime timestamp = LocalDateTime.parse(rs.getString("entry_timestamp").replace(" ", "T"), SQLITE_DATETIME_FORMATTER);
                boolean isDraft = rs.getInt("is_draft") == 1;

                // Use the full constructor to create the JournalEntry
                JournalEntry entry = new JournalEntry(id, content, timestamp, isDraft, title, mood);
                entries.add(entry);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des entrées récentes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeQuietly(rs);
            DatabaseManager.closeQuietly(stmt);
            DatabaseManager.closeQuietly(conn);
        }

        return entries;
    }

    /**
     * Récupère une entrée de journal par son ID
     * @param id ID de l'entrée à récupérer
     * @return L'entrée de journal complète ou null si non trouvée
     */
    public JournalEntry getJournalEntryById(int id) {
        String sql = "SELECT entry_id, entry_content, title, mood, entry_timestamp, is_draft FROM JournalEntries WHERE entry_id = ?";
        JournalEntry entry = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int entryId = rs.getInt("entry_id");
                String content = rs.getString("entry_content");
                String title = rs.getString("title");
                String mood = rs.getString("mood");
                String timestampStr = rs.getString("entry_timestamp");
                boolean isDraft = rs.getInt("is_draft") == 1;

                LocalDateTime timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"), SQLITE_DATETIME_FORMATTER);

                entry = new JournalEntry(entryId, content, timestamp, isDraft, title, mood);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving journal entry by ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeQuietly(rs);
            DatabaseManager.closeQuietly(pstmt);
            DatabaseManager.closeQuietly(conn);
        }

        return entry;
    }

    /**
     * Met à jour une entrée de journal existante
     * @param id ID de l'entrée à mettre à jour
     * @param content Nouveau contenu
     * @param title Nouveau titre
     * @param mood Nouvelle humeur
     * @return true si la mise à jour a réussi, sinon false
     */
    public boolean updateJournalEntry(int id, String content, String title, String mood) {
        String sql = "UPDATE JournalEntries SET entry_content = ?, title = ?, mood = ? WHERE entry_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, content);
            pstmt.setString(2, title);
            pstmt.setString(3, mood);
            pstmt.setInt(4, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating journal entry: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.closeQuietly(pstmt);
            DatabaseManager.closeQuietly(conn);
        }
    }

    /**
     * Récupère toutes les entrées de journal triées par date (plus récentes en premier)
     * @return Liste de toutes les entrées
     */
    public List<JournalEntry> getAllEntries() {
        String sql = "SELECT entry_id, entry_content, title, mood, entry_timestamp, is_draft FROM JournalEntries ORDER BY entry_timestamp DESC";
        List<JournalEntry> entries = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int entryId = rs.getInt("entry_id");
                String content = rs.getString("entry_content");
                String title = rs.getString("title");
                String mood = rs.getString("mood");
                String timestampStr = rs.getString("entry_timestamp");
                boolean isDraft = rs.getInt("is_draft") == 1;

                LocalDateTime timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"), SQLITE_DATETIME_FORMATTER);

                entries.add(new JournalEntry(entryId, content, timestamp, isDraft, title, mood));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all journal entries: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.closeQuietly(rs);
            DatabaseManager.closeQuietly(pstmt);
            DatabaseManager.closeQuietly(conn);
        }

        return entries;
    }
    /**
     * Deletes a journal entry from the database by its ID
     * @param id ID of the entry to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteJournalEntry(int id) {
        String sql = "DELETE FROM JournalEntries WHERE entry_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting journal entry: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            DatabaseManager.closeQuietly(pstmt);
            DatabaseManager.closeQuietly(conn);
        }
    }
}