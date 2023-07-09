package dev.terna.janelle.protocol;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Response {
    private final ResponseCode responseCode;
    private final String content;

    public Response(ResponseCode responseCode, String content) {
        this.responseCode = responseCode;
        this.content = content;
    }

    /**
     * Response format:
     * Response code (2 bytes) + Content length (2 bytes) + Content in UTF-8
     */
    public byte[] toByteArray() {
        byte[] contentBytes = new byte[]{};
        short contentLength = 0;
        if (!(content == null || "".equals(content))) {
            contentBytes = content.getBytes(StandardCharsets.UTF_8);
            contentLength = (short) contentBytes.length;
        }
        final var buffer = ByteBuffer.allocate(2 + 2 + contentLength);
        buffer.putShort(responseCode.getId());
        buffer.putShort(contentLength);
        if (contentLength > 0) {
            buffer.put(contentBytes);
        }
        return buffer.array();
    }
}
