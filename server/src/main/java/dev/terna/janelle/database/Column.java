package dev.terna.janelle.database;

import java.io.Serial;
import java.io.Serializable;

public class Column implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String name;
    private final DataType dataType;
    private final boolean isRequired;
    private final Object defaultValue;
    private int sizeInBytes;

    public Column(String name, DataType dataType, boolean isRequired, Object defaultValue) {
        this.name = name;
        this.dataType = dataType;
        this.isRequired = isRequired;
        this.defaultValue = defaultValue;

        sizeInBytes = dataType.getMaxSize();
    }

    public Column(String name, DataType dataType) {
        this(name, dataType, true, null);
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return sizeInBytes;
    }

    public void setSize(int sizeInBytes) {
        if (dataType == DataType.STRING) {
            this.sizeInBytes = sizeInBytes + 1; // Extra byte is for storing the length of the string.
        } else {
            this.sizeInBytes = sizeInBytes;
        }
    }

    public DataType getDataType() {
        return dataType;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public int getSizeInBytes() {
        return sizeInBytes;
    }

    public void validate(Object data) throws Exception {
        dataType.validate();


    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (object.getClass() != this.getClass()) {
            return false;
        }

        final var otherColumn = (Column) object;
        if (otherColumn.getName() == null || this.getName() == null) {
            return false;
        }

        if (otherColumn.getName() != this.getName()) {
            return false;
        }

        return true;
    }
}
