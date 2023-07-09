package dev.terna.janelle.server;

import dev.terna.janelle.protocol.ResponseCode;

import java.io.IOException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Drop requests when the work queue is full.
 */
public class RequestDropper implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        if (runnable instanceof RequestHandler requestHandler) {
            try {
                requestHandler.sendResponse(ResponseCode.TOO_MANY_REQUESTS, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}