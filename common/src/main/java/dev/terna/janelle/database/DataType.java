package dev.terna.janelle.database;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public enum DataType {
    INT(4, 4),
    FLOAT(4, 4),
    STRING(2, 256), // Max chars is actually 255 because the first byte is used to store the length of the string.
    BOOL(1, 1),
    ;

    int minSizeInBytes;
    int maxSizeInBytes;

    DataType(int minSizeInBytes, int maxSizeInBytes) {
        this.minSizeInBytes = minSizeInBytes;
        this.maxSizeInBytes = maxSizeInBytes;
    }

    public int getMinSize() {
        return minSizeInBytes;
    }

    public int getMaxSize() {
        return maxSizeInBytes;
    }

    public void validate() throws Exception {

    }

    public byte[] getBytes(Object data, int sizeInBytes) throws Exception {
        switch (this) {
            case INT:
                final int intData = Integer.parseInt(data.toString());
                return ByteBuffer.allocate(sizeInBytes).putInt(intData).array();

            case FLOAT:
                final float floatData = Float.parseFloat(data.toString());
                return ByteBuffer.allocate(sizeInBytes).putFloat(floatData).array();

            case STRING:
                final byte[] stringData = data.toString().getBytes(StandardCharsets.UTF_8);
                final byte[] bytes = new byte[sizeInBytes];
                bytes[0] = (byte) stringData.length;
                System.arraycopy(stringData, 0, bytes, 1, stringData.length);
                return bytes;

            case BOOL:
                final boolean boolData = Boolean.parseBoolean(data.toString());
                return new byte[] { (byte) (boolData ? 1 : 0) };

            default:
                throw new Exception("getBytes() not supported for " + this.name() + " type.");
        }
    }

    public Object getData(byte[] bytes) throws Exception {
        switch (this) {
            case INT:
                return ByteBuffer.wrap(bytes).getInt();

            case FLOAT:
                return ByteBuffer.wrap(bytes).getFloat();

            case STRING:
                final int stringLength = bytes[0];
                if (stringLength == 0) {
                    return null;
                }

                final var stringBytes = Arrays.copyOfRange(bytes, 1, stringLength + 1);
                return new String(stringBytes, StandardCharsets.UTF_8);

            case BOOL:
                return ByteBuffer.wrap(bytes).getShort() == 1 ? true : false;

            default:
                throw new Exception("getData() not supported for " + this.name() + " type.");
        }
    }
}
