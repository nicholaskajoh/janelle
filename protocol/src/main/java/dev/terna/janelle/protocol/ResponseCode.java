package dev.terna.janelle.protocol;

import java.util.Arrays;

public enum ResponseCode {
    SUCCESS((short) 1),
    INVALID_REQUEST((short)2),
    SERVER_ERROR((short)3),
    TOO_MANY_REQUESTS((short)4),
    QUERY_ERROR((short)5),
    INTERNAL_ERROR((short)6)
    ;

    private final short id;

    ResponseCode(short id) {
        this.id = id;
    }

    public short getId() {
        return id;
    }

    public static ResponseCode fromId(short id) {
        return Arrays.stream(ResponseCode.values()).filter(rc -> rc.getId() == id).findFirst().orElse(null);
    }
}
