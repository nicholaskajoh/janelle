package dev.terna.janelle.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.*;

public class Server {
    private final int port;
    private final int numThreads;
    private final int workQueueSize;

    public Server(int port, int numThreads, int workQueueSize) {
        this.port = port;
        this.numThreads = numThreads;
        this.workQueueSize = workQueueSize;
    }

    public void start() throws IOException {
        ExecutorService executor = new ThreadPoolExecutor(numThreads, numThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(workQueueSize), new RequestDropper());

        try(ServerSocket server = new ServerSocket(port)) {
            System.out.println("*** JANELLE DB SERVER ***");
            System.out.printf("Listening on port %s...\n\n", port);

            while (true) {
                final var requestHandler = new RequestHandler(server.accept());
                executor.submit(requestHandler);
            }
        }
    }
}
