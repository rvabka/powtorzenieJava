package org.example.server;

import javax.imageio.ImageIO; // Importuje klasę do obsługi obrazów
import java.awt.*; // Importuje klasy do rysowania grafiki
import java.awt.image.BufferedImage; // Importuje klasę do buforowania obrazów
import java.io.ByteArrayOutputStream; // Importuje klasę do pracy z strumieniami bajtów
import java.io.IOException; // Importuje klasę do obsługi wyjątków wejścia/wyjścia
import java.net.ServerSocket; // Importuje klasę do tworzenia serwera
import java.net.Socket; // Importuje klasę do obsługi połączeń sieciowych
import java.sql.Connection; // Importuje klasę do obsługi połączeń z bazą danych
import java.sql.DriverManager; // Importuje klasę do zarządzania sterownikami baz danych
import java.sql.PreparedStatement; // Importuje klasę do tworzenia przygotowanych zapytań SQL
import java.sql.SQLException; // Importuje klasę do obsługi wyjątków SQL
import java.util.Arrays; // Importuje klasę do pracy z tablicami
import java.util.Base64; // Importuje klasę do kodowania/dekodowania Base64
import java.util.HashMap; // Importuje klasę HashMap do przechowywania danych w formacie klucz-wartość
import java.util.Map; // Importuje interfejs Map do pracy z mapami

public class EEGServer {
    private String URL = "jdbc:sqlite:C\\Users\\wikst\\Documents\\bazka\\bazka.db"; // URL bazy danych SQLite
    private ServerSocket serverSocket; // Serwerowy gniazdo sieciowe
    private boolean running = false; // Flaga do kontrolowania pracy serwera
    private final Map<String, ClientHandler> clientHandlers = new HashMap<>(); // Mapa do przechowywania obsługi klientów

    public static void main(String[] args) {
        EEGServer server = new EEGServer(); // Tworzy instancję serwera
        server.start(2345); // Uruchamia serwer na porcie 2345
    }

    public void setURL(String URL) {
        this.URL = URL; // Ustawia URL bazy danych
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port); // Tworzy gniazdo serwera na podanym porcie
            System.out.println("EEGServer started on port " + port); // Informuje o uruchomieniu serwera
            running = true; // Ustawia flagę na true, aby oznaczyć, że serwer działa
            while (running) {
                Socket socket = serverSocket.accept(); // Akceptuje nowe połączenie od klienta
                ClientHandler clientHandler = new ClientHandler(socket, this); // Tworzy nowy obiekt ClientHandler dla obsługi klienta
                new Thread(clientHandler).start(); // Uruchamia nowy wątek dla obsługi klienta
            }
            serverSocket.close(); // Zamykam gniazdo serwera po zakończeniu pracy
        } catch (IOException e) {
            throw new RuntimeException(e); // Rzuca wyjątek w przypadku błędu
        }
    }

    public void addClient(String userName, ClientHandler clientHandler) {
        clientHandlers.put(userName, clientHandler); // Dodaje klienta do mapy klientów
    }

    public void process(String message, String username) {
        int electrode = clientHandlers.get(username).getAndIncrease(); // Pobiera numer elektrody dla użytkownika i zwiększa go
        System.out.println("processing: " + message + " " + username + " electrode: " + electrode); // Informuje o przetwarzaniu wiadomości
        saveToDataBase(message, username, electrode); // Zapisuje dane do bazy danych
    }

    public void removeClient(String userName) {
        ClientHandler userThread = clientHandlers.remove(userName); // Usuwa klienta z mapy klientów
        if (userThread != null) {
            System.out.println("The user " + userName + " finished"); // Informuje o zakończeniu pracy klienta
        }
    }

    public void printUsers() {
        System.out.println("Users connected: " + clientHandlers.keySet()); // Wypisuje listę aktualnie podłączonych użytkowników
    }

    public void saveToDataBase(String message, String username, int electrode) {
        String chart = drawChart(message); // Tworzy wykres na podstawie wiadomości
        String insertSQL = "INSERT INTO user_eeg(username, electrode_number, image) VALUES(?, ?, ?)"; // Zapytanie SQL do wstawienia danych

        // Próbujemy połączyć się z bazą danych i wykonać zapytanie
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, username); // Ustawia nazwę użytkownika w zapytaniu
            pstmt.setInt(2, electrode); // Ustawia numer elektrody w zapytaniu
            pstmt.setString(3, chart); // Ustawia wykres (w formacie Base64) w zapytaniu
            pstmt.executeUpdate(); // Wykonuje zapytanie
            System.out.println("Data saved."); // Informuje o zapisaniu danych
        } catch (SQLException e) {
            System.out.println(e.getMessage()); // Wyświetla komunikat błędu w przypadku niepowodzenia
        }
    }

    private String drawChart(String message) {
        int height = 100; // Wysokość obrazu
        int width = 200; // Szerokość obrazu

        // Tworzy nowy buforowany obraz o określonych wymiarach i typie
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics(); // Pobiera kontekst graficzny dla obrazu

        g2d.setColor(Color.WHITE); // Ustawia kolor na biały
        g2d.fillRect(0, 0, width, height); // Wypełnia cały obraz kolorem białym

        g2d.setColor(Color.RED); // Ustawia kolor na czerwony
        // Rozdziela wiadomość na pojedyncze wartości i konwertuje je na tablicę double
        String[] datas = message.split(",");
        double[] data = Arrays.stream(datas)
                .mapToDouble(Double::parseDouble)
                .toArray();
        for (int i = 0; i < data.length; i++) {
            int y = height / 2 - (int) ((data[i])); // Oblicza pozycję y dla danej wartości
            g2d.drawRect(i, y, 1, 1); // Rysuje prostokąt o szerokości 1 piksela na obliczonej pozycji
        }
        return encodeImageToBase64(image); // Koduje obraz do formatu Base64 i zwraca jako string
    }

    private static String encodeImageToBase64(BufferedImage image) {
        // Koduje obraz do formatu Base64 i zwraca jako string
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos); // Zapisuje obraz do strumienia w formacie PNG
            byte[] imageBytes = baos.toByteArray(); // Pobiera bajty obrazu
            return Base64.getEncoder().encodeToString(imageBytes); // Koduje bajty do Base64 i zwraca jako string
        } catch (IOException e) {
            e.printStackTrace(); // Wyświetla stos błędów w przypadku wyjątku
            return null;
        }
    }

    public void stop() {
        running = false; // Ustawia flagę na false, aby zatrzymać serwer
    }
}
