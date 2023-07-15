package dev.terna.janelle;


public class Input {
    private InputType type;
    private String query = "";
    private String[] command;

    public InputType getType() {
        return type;
    }

    public String getQuery() {
        return query;
    }

    public String[] getCommand() {
        return command;
    }

    /**
     * Returns true if input is complete and false otherwise.
     */
    public boolean parse(String inputLine) {
        if (inputLine == null) {
            throw new IllegalStateException("Invalid input line.");
        }
        final var line = inputLine.trim().replaceAll(" +", " ");

        if (type == null && line.startsWith(".")) {
            type = InputType.COMMAND;
            command = line.substring(1).split(" ");
            return true;
        }

        if (line.endsWith("\\")) {
            query += " " + line.substring(0, line.length() - 1);
        } else {
            query += " " + line;
        }

        if (line.endsWith(";")) {
            type = InputType.QUERY;
            return true;
        }

        return false;
    }
}
