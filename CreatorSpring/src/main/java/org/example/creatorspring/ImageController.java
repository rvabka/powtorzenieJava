import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Controller
public class ImageController {

    private static final String URL = "jdbc:sqlite:C:\\Users\\luke\\Documents\\usereeg.db";

    @GetMapping("/image")
    public String image(@RequestParam String username, @RequestParam int electrode, Model model) {
        String image= null;
        String sql = "SELECT image FROM user_eeg WHERE username = ? AND electrode_number = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setInt(2, electrode);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    image = rs.getString("image");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        model.addAttribute("username", username);
        model.addAttribute("electrode", electrode);
        model.addAttribute("image", image);
        return "eegimage";
    }
}