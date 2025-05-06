package mindgarden.db;

import mindgarden.model.User;

import java.sql.*;

public class UserDAO {

    public boolean register(User user) {
        String sql = "INSERT INTO Users (username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword()); // En prod, cryptez ça !

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'enregistrement : " + e.getMessage());
            return false;
        }
    }

    public User login(String email, String password) {
        String sql = "SELECT * FROM Users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password")
                );
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la connexion : " + e.getMessage());
        }

        return null;
    }
}
