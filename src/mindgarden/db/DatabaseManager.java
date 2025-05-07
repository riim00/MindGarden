package mindgarden.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:C:/Users/pc/IdeaProjects/MindGarden/database/mindgarden.db";

    private static final String CREATE_USERS_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS Users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL,
            email TEXT NOT NULL UNIQUE,
            password TEXT NOT NULL
        );
    """;

    private static final String CREATE_MOOD_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS MoodEntries (
            entry_id INTEGER PRIMARY KEY AUTOINCREMENT,
            mood_type TEXT NOT NULL,
            entry_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
            notes TEXT
        );
    """;

    private static final String CREATE_JOURNAL_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS JournalEntries (
                      entry_id INTEGER PRIMARY KEY AUTOINCREMENT,
                      entry_content TEXT NOT NULL,
                      is_draft INTEGER DEFAULT 0,
                      title TEXT,
                      mood TEXT,
                      entry_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                  );
    """;

    public static Connection connect() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            System.out.println("✅ Connexion établie avec la base : " + DB_URL);
            createTablesIfNotExist(conn);
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base : " + e.getMessage());
            throw e;
        }
    }

    private static void createTablesIfNotExist(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_USERS_TABLE_SQL);
            System.out.println("✅ Table Users vérifiée/créée.");
            stmt.execute(CREATE_MOOD_TABLE_SQL);
            System.out.println("✅ Table MoodEntries vérifiée/créée.");
            stmt.execute(CREATE_JOURNAL_TABLE_SQL);
            System.out.println("✅ Table JournalEntries vérifiée/créée.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création des tables : " + e.getMessage());
        }
    }

    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                System.err.println("⚠️ Erreur lors de la fermeture : " + e.getMessage());
            }
        }
    }
}
