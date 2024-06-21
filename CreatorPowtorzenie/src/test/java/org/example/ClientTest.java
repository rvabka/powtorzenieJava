package org.example;

import org.example.client.Client;
import org.example.databasecreator.Creator;
import org.example.server.EEGServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientTest {
    private static EEGServer eegServer; // Serwer EEG
    private static Creator creator = new Creator(); // Obiekt do tworzenia i zarządzania bazą danych
    private static final String URL = "jdbc:sqlite:C:\\Users\\luke\\Documents\\usereegtest.db"; // URL do bazy danych

    // Metoda uruchamiana przed wszystkimi testami
    @BeforeAll
    public static void setUp() {
        creator.create(URL); // Utworzenie bazy danych
        eegServer = new EEGServer(); // Inicjalizacja serwera EEG
        eegServer.setURL(URL); // Ustawienie URL bazy danych w serwerze EEG
        new Thread(() -> eegServer.start(2345)).start(); // Uruchomienie serwera EEG w nowym wątku
    }

    // Metoda uruchamiana po wszystkich testach
    @AfterAll
    public static void stop() {
        eegServer.stop(); // Zatrzymanie serwera EEG
        creator.delete(URL); // Usunięcie bazy danych
    }

    // Metoda pobierająca obraz z bazy danych dla danego użytkownika i elektrody
    public String getImage(String username, int electrode) {
        String image = null; // Zmienna do przechowywania obrazu
        String sql = "SELECT image FROM user_eeg WHERE username = ? AND electrode_number = ?"; // Zapytanie SQL

        // Blok try-with-resources do zarządzania zasobami bazy danych
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username); // Ustawienie parametru username w zapytaniu SQL
            pstmt.setInt(2, electrode); // Ustawienie parametru electrode w zapytaniu SQL

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    image = rs.getString("image"); // Pobranie obrazu z wyników zapytania
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Obsługa wyjątków SQL
        }
        return image; // Zwrócenie obrazu
    }
}
