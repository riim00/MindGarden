package mindgarden.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    // ✅ Chemin absolu vers le fichier SQLite que tu veux utiliser
    private static final String DB_URL = "jdbc:sqlite:C:/Users/rimta/MindGarden/database/mindgarden.db";

    // --- Table Creation SQL ---
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
                entry_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;
    // --- End Table Creation SQL ---

    /**
     * Établit une connexion à la base SQLite.
     * Crée la base et les tables si elles n'existent pas encore.
     *
     * @return une connexion active à la base de données.
     * @throws SQLException en cas d’erreur d’accès à la base.
     */
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

    /**
     * Crée les tables nécessaires si elles n'existent pas.
     *
     * @param conn la connexion active.
     */
    private static void createTablesIfNotExist(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_MOOD_TABLE_SQL);
            System.out.println("✅ Table MoodEntries vérifiée/créée.");
            stmt.execute(CREATE_JOURNAL_TABLE_SQL);
            System.out.println("✅ Table JournalEntries vérifiée/créée.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création des tables : " + e.getMessage());
        }
    }

    /**
     * Ferme un élément AutoCloseable (comme ResultSet ou Statement) sans exception.
     *
     * @param resource l’objet à fermer.
     */
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
