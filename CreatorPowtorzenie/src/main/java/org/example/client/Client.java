package org.example.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Client {
    // Inicjalizacja obiektu ServerHandler dla połączenia z serwerem localhost na porcie 2345
    ServerHandler serverHandler = new ServerHandler("localhost", 2345);

    public static void main(String[] args) {
        // Tworzenie instancji klienta
        Client client = new Client();

        // Odczyt nazwy i ścieżki pliku z klawiatury
        Scanner fromKeyboard = new Scanner(System.in);
        String name = fromKeyboard.nextLine();       // Wczytanie nazwy
        String filepath = fromKeyboard.nextLine();   // Wczytanie ścieżki pliku

        // Wywołanie metody do wysyłania danych na serwer
        client.sendData(name, filepath);
    }

    // Metoda do wysyłania danych na serwer
    public void sendData(String name, String filepath) {
        try {
            // Wysłanie nazwy na serwer
            serverHandler.send(name);

            // Utworzenie skanera pliku do odczytu
            Scanner filescanner = new Scanner(new File(filepath));

            // Odczytanie i wysłanie kolejnych linii z pliku
            while(filescanner.hasNextLine()) {
                String line = filescanner.nextLine();   // Odczyt linii
                serverHandler.send(line);               // Wysłanie linii do serwera
                Thread.sleep(2000);               // Oczekiwanie 2 sekundy
            }
        } catch (FileNotFoundException e) {
            // Rzucenie RuntimeException w przypadku błędu braku pliku
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            // Rzucenie RuntimeException w przypadku przerwania wątku
            throw new RuntimeException(e);
        }
    }
}
