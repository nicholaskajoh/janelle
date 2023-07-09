package dev.terna.janelle;

import dev.terna.janelle.protocol.Request;
import dev.terna.janelle.protocol.RequestType;
import dev.terna.janelle.protocol.ResponseCode;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    private static final String janelleAsciiText = """
   ___                      _  _      \s
  |_  |                    | || |     \s
    | |  __ _  _ __    ___ | || |  ___\s
    | | / _` || '_ \\  / _ \\| || | / _ \\
/\\__/ /| (_| || | | ||  __/| || ||  __/
\\____/  \\__,_||_| |_| \\___||_||_| \\___|
                                      \s
                                      \s
""";

    public static void main(String[] args) throws IOException {
        System.out.println(janelleAsciiText);
        System.out.println("Welcome to Janelle SQL database created by Nicholas Kajoh. \n Type a command/query or \".HELP\" for more info.");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();

            final var input = new Input();
            final var done = input.parse(line);

            if (!done) {
                continue;
            }

            if (input.getType() == InputType.COMMAND) {
                if (input.getCommand().length == 0) {
                    System.out.println("Invalid input.");
                    continue;
                }

                Command command;
                try {
                    command = Command.valueOf(input.getCommand()[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid or unsupported command: " + input.getCommand()[0]);
                    continue;
                }

                boolean exit = false;
                switch (command) {
                    case EXIT -> exit = true;

                    case HELP -> {

                    }

                    case PING -> sendRequest(RequestType.PING, null);
                }

                if (exit) {
                    break;
                }
            } else if (input.getType() == InputType.QUERY) {
                sendRequest(RequestType.QUERY, input.getQuery());
            } else {
                System.out.println("Failed to parse input. :(");
            }
        }

        System.out.println("Bye bye! :)");
    }

    private static Socket getClient() throws IOException {
        return new Socket("127.0.0.1", 6969);
    }

    private static ResponseCode getResponseCode(Socket client) throws IOException {
        final var idBytes = client.getInputStream().readNBytes(2);
        final var id = ByteBuffer.wrap(idBytes).getShort();
        return ResponseCode.fromId(id);
    }

    private static String getContent(Socket client) throws IOException {
        final var contentLengthBytes = client.getInputStream().readNBytes(2);
        final var contentLength = ByteBuffer.wrap(contentLengthBytes).getShort();
        if (contentLength == 0) {
            return null;
        }

        final var contentBytes = client.getInputStream().readNBytes(contentLength);
        return new String(contentBytes, StandardCharsets.UTF_8);
    }

    private static void sendRequest(RequestType type, String requestContent) throws IOException {
        final var client = getClient();
        final var request = new Request(type, requestContent).toByteArray();
        client.getOutputStream().write(request);

        final var responseCode = getResponseCode(client);
        final var responseContent = getContent(client);
        if (responseCode == ResponseCode.SUCCESS) {
            System.out.println(responseContent);
        } else {
            System.out.println(responseCode.name() + " " + responseContent);
        }
    }
}
