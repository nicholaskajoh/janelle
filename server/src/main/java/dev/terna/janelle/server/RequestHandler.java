package dev.terna.janelle.server;

import dev.terna.janelle.database.Database;
import dev.terna.janelle.database.Utils;
import dev.terna.janelle.protocol.Request;
import dev.terna.janelle.protocol.RequestType;
import dev.terna.janelle.protocol.Response;
import dev.terna.janelle.protocol.ResponseCode;
import dev.terna.janelle.sql.Emitter;
import dev.terna.janelle.sql.Parser;
import dev.terna.janelle.sql.Query;
import dev.terna.janelle.sql.Tokenizer;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public class RequestHandler implements Runnable {
    private final Socket client;

    public RequestHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            var requestType = getRequestType();
            if (requestType == null) {
                sendResponse(ResponseCode.INVALID_REQUEST, "Invalid request type.");
                return;
            }
            String queryString;
            try {
                queryString = getContent();
            } catch (IllegalStateException e) {
                sendResponse(ResponseCode.INVALID_REQUEST, e.getMessage());
                return;
            }
            System.out.println("[" + new Date() + "] Request: " + requestType.name() + " " + queryString);

            switch (requestType) {
                case PING -> sendResponse(ResponseCode.SUCCESS, "PONG");

                case QUERY -> {
                    List<Query> queries;
                    try {
                        queries = getQueries(queryString);
                    } catch (Exception e) {
                        sendResponse(ResponseCode.QUERY_ERROR, e.getMessage());
                        return;
                    }

                    try {
                        final var db = new Database();
                        StringBuilder resultStringBuilder = new StringBuilder();
                        for (var query : queries) {
                            final var result = db.processQuery(query);
                            resultStringBuilder.append(Utils.renderQueryResult(result)).append("\n");
                        }
                        sendResponse(ResponseCode.SUCCESS, resultStringBuilder.toString());

                    } catch (Exception e) {
                        sendResponse(ResponseCode.INTERNAL_ERROR, e.getMessage());
                    }
                }

                default -> sendResponse(ResponseCode.SERVER_ERROR, "Server error: Request type not implemented.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendResponse(ResponseCode responseCode, String content) throws IOException {
        System.out.println("[" + new Date() + "] Response: " + responseCode.name() + " " + content);

        final var bytes = new Response(responseCode, content).toByteArray();
        client.getOutputStream().write(bytes);
        client.close();
    }

    private RequestType getRequestType() throws IOException {
        final var codeBytes = client.getInputStream().readNBytes(2);
        if (codeBytes.length != 2) {
            return null;
        }
        final var code = ByteBuffer.wrap(codeBytes).getShort();
        return RequestType.fromCode(code);
    }

    private String getContent() throws IOException {
        final var contentLengthBytes = client.getInputStream().readNBytes(2);
        if (contentLengthBytes.length != 2) {
            return null;
        }
        final var contentLength = ByteBuffer.wrap(contentLengthBytes).getShort();
        if (contentLength == 0) {
            return null;
        }
        if (contentLength > Request.MAX_CONTENT_LENGTH) {
            throw new IllegalStateException("Request too large!");
        }

        final var contentBytes = client.getInputStream().readNBytes(contentLength);
        if (contentBytes.length != contentLength) {
            throw new IllegalStateException("Incomplete request content.");
        }
        return new String(contentBytes, StandardCharsets.UTF_8);
    }

    private List<Query> getQueries(String queryString) throws Exception {
        final var tokenizer = new Tokenizer(queryString);
        final var tokens = tokenizer.tokenize();
        final var parser = new Parser();
        final var ast = parser.parse(tokens);
        final var emitter = new Emitter();
        return emitter.emit(ast);
    }
}
