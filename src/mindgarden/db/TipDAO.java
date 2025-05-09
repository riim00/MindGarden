package mindgarden.db;

import mindgarden.model.Tip;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TipDAO {

    public static List<Tip> getAllTips() {
        List<Tip> tips = new ArrayList<>();

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM tips")) {

            while (rs.next()) {
                tips.add(new Tip(rs.getInt("id"), rs.getString("content")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tips;
    }
}
