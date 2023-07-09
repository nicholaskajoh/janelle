package dev.terna.janelle.protocol;

import java.util.Arrays;

public enum RequestType {
    PING((short) 1),
    QUERY((short) 2);

    private final short code;

    RequestType(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
    }

    public static RequestType fromCode(short code) {
        return Arrays.stream(RequestType.values()).filter(rt -> rt.getCode() == code).findFirst().orElse(null);
    }
}
