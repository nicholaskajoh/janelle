package dev.terna.janelle.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Request {
    public static final short MAX_CONTENT_LENGTH = 10240;
    private final RequestType type;
    private final String content;

    public Request(RequestType type, String content) {
        this.type = type;
        this.content = content;
    }

    /**
     * Request format:
     * Request type (2 bytes) + Content length (2 bytes) + Content in UTF-8
     */
    public byte[] toByteArray() {
        byte[] contentBytes = new byte[]{};
        short contentLength = 0;
        if (!(content == null || "".equals(content))) {
            contentBytes = content.getBytes(StandardCharsets.UTF_8);
            contentLength = (short) contentBytes.length;
        }
        final var buffer = ByteBuffer.allocate(2 + 2 + contentLength);
        buffer.putShort(type.getCode());
        buffer.putShort(contentLength);
        if (contentLength > 0) {
            buffer.put(contentBytes);
        }
        return buffer.array();
    }
}
