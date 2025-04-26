package mindgarden.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;

public class DatabaseManager {

    private static final String DB_FOLDER_PATH = "database"; // Relative to project root
    private static final String DB_NAME = "mindgarden.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FOLDER_PATH + File.separator + DB_NAME;

    // --- Table Creation SQL ---
    private static final String CREATE_MOOD_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS MoodEntries (
                entry_id INTEGER PRIMARY KEY AUTOINCREMENT,
                mood_type TEXT NOT NULL,
                entry_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
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


    // Ensure the database directory exists
    static {
        File dbFolder = new File(DB_FOLDER_PATH);
        if (!dbFolder.exists()) {
            if (dbFolder.mkdirs()) {
                System.out.println("Database directory created: " + dbFolder.getAbsolutePath());
            } else {
                System.err.println("Failed to create database directory: " + dbFolder.getAbsolutePath());
                // Consider throwing an exception or handling this more robustly
            }
        } else {
             System.out.println("Database directory already exists: " + dbFolder.getAbsolutePath());
        }
    }


    /**
     * Establishes a connection to the SQLite database.
     * Creates the database file and necessary tables if they don't exist.
     *
     * @return A Connection object to the database.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection connect() throws SQLException {
        Connection conn = null;
        try {
            // Load the SQLite JDBC driver (optional for modern JDBC)
            // Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection(DB_URL);
            System.out.println("Connection to SQLite established: " + DB_URL);

            // Create tables if they don't exist
            createTablesIfNotExist(conn);

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            throw e; // Re-throw the exception to be handled by the caller
        }
        return conn;
    }

    /**
     * Creates the database tables if they do not already exist.
     *
     * @param conn The database connection.
     */
    private static void createTablesIfNotExist(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Execute CREATE TABLE statements
            stmt.execute(CREATE_MOOD_TABLE_SQL);
            System.out.println("Checked/Created MoodEntries table.");

            stmt.execute(CREATE_JOURNAL_TABLE_SQL);
            System.out.println("Checked/Created JournalEntries table.");

            // Add more table creations here if needed in the future

        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            // Consider more robust error handling
        }
    }

    // Optional: Add methods for closing resources if needed frequently outside DAOs
    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                // Log or ignore
                System.err.println("Error closing resource: " + e.getMessage());
            }
        }
    }
}
