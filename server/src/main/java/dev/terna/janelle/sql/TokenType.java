package dev.terna.janelle.sql;

public enum TokenType {
    // Keywords
    CREATE,
    INSERT,
    SELECT,
    ALTER,
    UPDATE,
    ADD,
    DROP,
    DELETE,
    DESCRIBE,
    ORDER_BY,
    SET,
    FROM,
    TO,
    INTO,
    TABLE,
    COLUMN,
    VALUES,
    WHERE,
    ALL,
    REQUIRED,
    NULLABLE,
    DEFAULT,
    ASCENDING,
    DESCENDING,
    BEGIN,
    COMMIT,
    ROLLBACK,

    // Functions
    COUNT,
    AVERAGE,
    SUM,

    // Data types
    INT_TYPE,
    FLOAT_TYPE,
    STRING_TYPE,
    BOOL_TYPE,

    IDENTIFIER,

    // Literals
    INT_LITERAL,
    FLOAT_LITERAL,
    STRING_LITERAL,
    BOOL_LITERAL,
    NULL_LITERAL,

    // Operators
    ADD_OP,
    SUBTRACT_OP,
    MULTIPLY_OP,
    DIVIDE_OP,
    EQUAL_OP,
    NOT_EQUAL_OP,
    GREATER_THAN_OP,
    GREATER_THAN_OR_EQUAL_OP,
    LESS_THAN_OP,
    LESS_THAN_OR_EQUAL_OP,
    AND,
    OR,

    // Punctuators
    OPEN_PAREN,
    CLOSE_PAREN,
    SEMICOLON,
    COMMA,
}
