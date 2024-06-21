package org.example.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    private final Socket socket; // Socket połączenia z klientem
    private final EEGServer server; // Odwołanie do serwera, który zarządza klientami
    private final Scanner input; // Scanner do odczytywania danych wejściowych od klienta
    private String clientName; // Nazwa klienta
    private int nelectrode; // Licznik elektrod, który będzie inkrementowany

    // Konstruktor klasy ClientHandler
    public ClientHandler(Socket socket, EEGServer server) {
        try {
            this.socket = socket; // Inicjalizacja socketu
            this.server = server; // Inicjalizacja serwera
            input = new Scanner(socket.getInputStream()); // Inicjalizacja scannera do odczytu z wejścia socketu
        } catch (IOException e) {
            throw new RuntimeException(e); // Rzucenie wyjątku w przypadku problemów z IO
        }
    }

    // Metoda uruchomienia wątku
    @Override
    public void run() {
        try {
            // Sprawdzenie, czy klient jest połączony
            boolean connected = isClinetConnected();

            if (!connected) {
                return; // Jeśli nie jest połączony, zakończ wątek
            }

            // Dołączenie klienta do serwera
            join();
            // Komunikacja z klientem
            comunicate();
            // Odłączenie klienta
            disconnect();

        } catch (IOException ex) {
            ex.printStackTrace(); // Obsługa wyjątków wejścia-wyjścia
        }
    }

    // Metoda sprawdzająca, czy klient jest połączony
    private boolean isClinetConnected() throws IOException {
        if (input.hasNextLine()) {
            clientName = input.nextLine(); // Pobranie nazwy klienta
            return true;
        }
        return false;
    }

    // Metoda dodająca klienta do serwera
    private void join() {
        server.addClient(clientName, this); // Dodanie klienta do listy klientów na serwerze
        server.printUsers(); // Wydrukowanie listy użytkowników
    }

    // Metoda obsługująca komunikację z klientem
    private void comunicate() throws IOException {
        String clientMessage;
        boolean connected = true;

        while (connected) {
            if (input.hasNextLine()) {
                clientMessage = input.nextLine(); // Odczytanie wiadomości od klienta
                connected = parseClientMessage(clientMessage); // Parsowanie wiadomości od klienta
            } else {
                connected = false; // Jeśli brak danych, zakończenie pętli
            }
        }
    }

    // Metoda parsująca wiadomości od klienta
    public boolean parseClientMessage(String clientMessage) {
        if (!clientMessage.equalsIgnoreCase("bye")) {
            server.process(clientMessage, clientName); // Przetwarzanie wiadomości przez serwer
            return true;
        }
        return false; // Zakończenie połączenia w przypadku wiadomości "bye"
    }

    // Metoda odłączająca klienta
    private void disconnect() throws IOException {
        server.removeClient(clientName); // Usunięcie klienta z listy na serwerze
        socket.close(); // Zamknięcie socketu
        server.printUsers(); // Wydrukowanie listy użytkowników
    }

    // Metoda zwracająca bieżącą wartość licznika elektrod i inkrementująca licznik
    public int getAndIncrease() {
        int i = this.nelectrode; // Pobranie bieżącej wartości licznika
        nelectrode++; // Inkrementacja licznika
        return i; // Zwrócenie poprzedniej wartości licznika
    }
}