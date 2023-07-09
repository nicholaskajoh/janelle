package dev.terna.janelle.server;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        final var server = new Server(6969, 5, 20);
        server.start();
    }
}
