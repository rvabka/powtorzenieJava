package org.example.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerHandler {
    private Socket socket;        // Obiekt Socket do nawiązania połączenia
    private PrintWriter output;   // Obiekt PrintWriter do wysyłania danych do serwera

    // Konstruktor klasy ServerHandler, inicjalizuje połączenie z serwerem
    public ServerHandler(String hostname, int port) {
        try {
            this.socket = new Socket(hostname, port);   // Utworzenie socketu dla określonego hosta i portu
            this.output = new PrintWriter(socket.getOutputStream(), true);  // Inicjalizacja obiektu do wysyłania danych
        } catch (UnknownHostException e) {
            // Rzucenie RuntimeException w przypadku nieznanej nazwy hosta
            throw new RuntimeException(e);
        } catch (IOException e) {
            // Rzucenie RuntimeException w przypadku błędu wejścia/wyjścia
            throw new RuntimeException(e);
        }
    }

    // Metoda do wysyłania wiadomości do serwera
    public void send(String message) {
        output.println(message);   // Wysłanie wiadomości przez obiekt PrintWriter
    }

    // Metoda zamykająca połączenie z serwerem
    public void close() {
        try {
            socket.close();   // Zamknięcie socketu
        } catch (IOException e) {
            // Rzucenie RuntimeException w przypadku błędu wejścia/wyjścia przy zamykaniu
            throw new RuntimeException(e);
        }
    }

}
