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
     * @param userId The ID of the user who created this entry
     * @return true if the insertion was successful, false otherwise.
     */
    public boolean addJournalEntry(String content, String title, String mood, int userId) {
        String sql = "INSERT INTO JournalEntries(entry_content, is_draft, title, mood, user_id) VALUES(?, 0, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, content);
            pstmt.setString(2, title);
            pstmt.setString(3, mood);
            pstmt.setInt(4, userId);
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

    public boolean addJournalEntryAsDraft(String content, String title, String mood, int userId) {
        String sql = "INSERT INTO JournalEntries(entry_content, is_draft, title, mood, user_id) VALUES(?, 1, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, content);
            pstmt.setString(2, title);
            pstmt.setString(3, mood);
            pstmt.setInt(4, userId);
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
     * Retrieves the most recent journal entries from the database for a specific user.
     *
     * @param limit Number of entries to retrieve
     * @param userId The ID of the user whose entries to retrieve
     * @return A list of JournalEntry objects. Returns an empty list if none are found.
     */
    public List<JournalEntry> getRecentJournalEntries(int limit, int userId) {
        String sql = "SELECT entry_id, entry_content, entry_timestamp, is_draft, title, mood, user_id FROM JournalEntries " +
                "WHERE is_draft = 0 AND user_id = ? ORDER BY entry_timestamp DESC LIMIT ?";
        List<JournalEntry> entries = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("entry_id");
                String content = rs.getString("entry_content");
                String timestampStr = rs.getString("entry_timestamp");
                boolean isDraft = rs.getInt("is_draft") == 1;
                String title = rs.getString("title");
                String mood = rs.getString("mood");
                int entryUserId = rs.getInt("user_id");

                LocalDateTime timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"), SQLITE_DATETIME_FORMATTER);

                entries.add(new JournalEntry(id, content, timestamp, isDraft, title, mood, entryUserId));
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
     * Récupère les entrées de journal les plus récentes pour un utilisateur spécifique
     * @param limit Le nombre d'entrées à récupérer
     * @param userId L'ID de l'utilisateur
     * @return Une liste des entrées de journal les plus récentes
     */
    public List<JournalEntry> getRecentEntries(int limit, int userId) {
        List<JournalEntry> entries = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.connect();
            String sql = "SELECT entry_id, entry_content, title, mood, entry_timestamp, is_draft, user_id " +
                    "FROM JournalEntries " +
                    "WHERE is_draft = 0 AND user_id = ? " +
                    "ORDER BY entry_timestamp DESC " +
                    "LIMIT ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("entry_id");
                String content = rs.getString("entry_content");
                String title = rs.getString("title");
                String mood = rs.getString("mood");
                LocalDateTime timestamp = LocalDateTime.parse(rs.getString("entry_timestamp").replace(" ", "T"), SQLITE_DATETIME_FORMATTER);
                boolean isDraft = rs.getInt("is_draft") == 1;
                int entryUserId = rs.getInt("user_id");

                // Use the full constructor to create the JournalEntry
                JournalEntry entry = new JournalEntry(id, content, timestamp, isDraft, title, mood, entryUserId);
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
    public List<JournalEntry> getRecentEntriesForUser(int limit, int userId) {
        List<JournalEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM JournalEntries WHERE user_id = ? ORDER BY entry_timestamp DESC LIMIT ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, limit);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JournalEntry entry = new JournalEntry(
                        rs.getInt("entry_id"),
                        rs.getString("entry_content"),
                        rs.getTimestamp("entry_timestamp").toLocalDateTime(),
                        rs.getBoolean("is_draft"),
                        rs.getString("title"),
                        rs.getString("mood"),
                        rs.getInt("user_id")
                );
                entries.add(entry);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving recent entries: " + e.getMessage());
        }

        return entries;
    }

    /**
     * Récupère une entrée de journal par son ID et vérifie qu'elle appartient à l'utilisateur
     * @param id ID de l'entrée à récupérer
     * @param userId ID de l'utilisateur
     * @return L'entrée de journal complète ou null si non trouvée ou si elle n'appartient pas à l'utilisateur
     */
    public JournalEntry getJournalEntryById(int id, int userId) {
        String sql = "SELECT entry_id, entry_content, title, mood, entry_timestamp, is_draft, user_id FROM JournalEntries WHERE entry_id = ? AND user_id = ?";
        JournalEntry entry = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int entryId = rs.getInt("entry_id");
                String content = rs.getString("entry_content");
                String title = rs.getString("title");
                String mood = rs.getString("mood");
                String timestampStr = rs.getString("entry_timestamp");
                boolean isDraft = rs.getInt("is_draft") == 1;
                int entryUserId = rs.getInt("user_id");

                LocalDateTime timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"), SQLITE_DATETIME_FORMATTER);

                entry = new JournalEntry(entryId, content, timestamp, isDraft, title, mood, entryUserId);
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
     * @param userId ID de l'utilisateur qui possède l'entrée
     * @return true si la mise à jour a réussi, sinon false
     */
    public boolean updateJournalEntry(int id, String content, String title, String mood, int userId) {
        String sql = "UPDATE JournalEntries SET entry_content = ?, title = ?, mood = ? WHERE entry_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, content);
            pstmt.setString(2, title);
            pstmt.setString(3, mood);
            pstmt.setInt(4, id);
            pstmt.setInt(5, userId);

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
     * Récupère toutes les entrées de journal d'un utilisateur triées par date (plus récentes en premier)
     * @param userId ID de l'utilisateur
     * @return Liste de toutes les entrées de l'utilisateur
     */
    public List<JournalEntry> getAllEntries(int userId) {
        String sql = "SELECT entry_id, entry_content, title, mood, entry_timestamp, is_draft, user_id FROM JournalEntries WHERE user_id = ? ORDER BY entry_timestamp DESC";
        List<JournalEntry> entries = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int entryId = rs.getInt("entry_id");
                String content = rs.getString("entry_content");
                String title = rs.getString("title");
                String mood = rs.getString("mood");
                String timestampStr = rs.getString("entry_timestamp");
                boolean isDraft = rs.getInt("is_draft") == 1;
                int entryUserId = rs.getInt("user_id");

                LocalDateTime timestamp = LocalDateTime.parse(timestampStr.replace(" ", "T"), SQLITE_DATETIME_FORMATTER);

                entries.add(new JournalEntry(entryId, content, timestamp, isDraft, title, mood, entryUserId));
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
     * Deletes a journal entry from the database by its ID and verifies user ownership
     * @param id ID of the entry to delete
     * @param userId ID of the user who should own the entry
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteJournalEntry(int id, int userId) {
        String sql = "DELETE FROM JournalEntries WHERE entry_id = ? AND user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseManager.connect();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);

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