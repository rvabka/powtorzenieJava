package org.example.databasecreator;

import java.io.File; // Importuje klasę do pracy z plikami
import java.sql.Connection; // Importuje klasę do obsługi połączeń z bazą danych
import java.sql.DriverManager; // Importuje klasę do zarządzania sterownikami baz danych
import java.sql.SQLException; // Importuje klasę do obsługi wyjątków SQL
import java.sql.Statement; // Importuje klasę do wykonywania statycznych zapytań SQL

public class Creator {

    public static void main(String[] args) {
        String url = "jdbc:sqlite:C\\Users\\wikst\\Documents\\bazka\\bazka.db"; // URL do bazy danych SQLite
        Creator creator = new Creator(); // Tworzy instancję klasy Creator
        creator.create(url); // Wywołuje metodę create z URL bazy danych
    }

    public void create(String url) {
        // Zapytanie SQL do utworzenia tabeli, jeśli nie istnieje
        String createTableSQL = "CREATE TABLE IF NOT EXISTS user_eeg ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT," // Kolumna id z autoincrementem
                + "username TEXT NOT NULL," // Kolumna username, tekstowa, nie może być NULL
                + "electrode_number INTEGER NOT NULL," // Kolumna electrode_number, liczba całkowita, nie może być NULL
                + "image TEXT NOT NULL" // Kolumna image, tekstowa, nie może być NULL
                + ");";

        // Próbujemy połączyć się z bazą danych i wykonać zapytanie
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL); // Wykonuje zapytanie do utworzenia tabeli
            System.out.println("Ok"); // Informuje o pomyślnym utworzeniu tabeli
        } catch (SQLException e) {
            System.out.println(e.getMessage()); // Wyświetla komunikat błędu w przypadku niepowodzenia
        }
    }

    public void delete(String url) {
        // Pobiera ścieżkę pliku z URL bazy danych
        String filepath = url.substring(url.indexOf("\\"));
        File dbFile = new File(filepath); // Tworzy obiekt File dla bazy danych
        if (dbFile.exists()) {
            // Próbuje usunąć plik bazy danych, jeśli istnieje
            if (!dbFile.delete()) {
                System.out.println("Error during delete database"); // Informuje o błędzie podczas usuwania bazy danych
            }
        } else {
            System.out.println("Error database doesn't exist"); // Informuje, że baza danych nie istnieje
        }
    }
}
