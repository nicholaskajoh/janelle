package dev.terna.janelle.sql;

import java.util.*;
import java.util.regex.Pattern;

public class Tokenizer {
    private final String query;
    private int queryBufferStart = 0;
    private int queryBufferEnd = 1;
    private final List<Token> tokens = new ArrayList<>();

    private static final Set<TokenType> NUMERIC_LITERALS = Set.of(TokenType.INT_LITERAL, TokenType.FLOAT_LITERAL);
    private static final Map<String, TokenType> COMPARISON_OPS = new HashMap<>();
    static {
        COMPARISON_OPS.put(">", TokenType.GREATER_THAN_OP);
        COMPARISON_OPS.put("<", TokenType.LESS_THAN_OP);
        COMPARISON_OPS.put(">=", TokenType.GREATER_THAN_OR_EQUAL_OP);
        COMPARISON_OPS.put("<=", TokenType.LESS_THAN_OR_EQUAL_OP);
        COMPARISON_OPS.put("=", TokenType.EQUAL_OP);
        COMPARISON_OPS.put("!=", TokenType.NOT_EQUAL_OP);
    }

    public Tokenizer(String query) {
        if (query == null || query.trim().equals("")) {
            throw new IllegalArgumentException("Syntax error: Invalid query!");
        }
        this.query = query;
    }

    public List<Token> tokenize() {
        while (queryBufferEnd <= query.length()) {
            if (
                    // Must be above all tokenizers.
                    ignoreWhiteSpace()
                    || ignoreComment()

                    || tokenizeKeyword("^create$", TokenType.CREATE)
                    || tokenizeKeyword("^insert$", TokenType.INSERT)
                    || tokenizeKeyword("^select$", TokenType.SELECT)
                    || tokenizeKeyword("^alter$", TokenType.ALTER)
                    || tokenizeKeyword("^update$", TokenType.UPDATE)
                    || tokenizeKeyword("^add$", TokenType.ADD)
                    || tokenizeKeyword("^drop$", TokenType.DROP)
                    || tokenizeKeyword("^delete$", TokenType.DELETE)
                    || tokenizeKeyword("^describe$", TokenType.DESCRIBE)
                    || tokenizeKeyword("^order\\s+by$", TokenType.ORDER_BY)
                    || tokenizeKeyword("^set$", TokenType.SET)
                    || tokenizeKeyword("^from$", TokenType.FROM)
                    || tokenizeKeyword("^to$", TokenType.TO)
                    || tokenizeKeyword("^into$", TokenType.INTO)
                    || tokenizeKeyword("^table$", TokenType.TABLE)
                    || tokenizeKeyword("^column$", TokenType.COLUMN)
                    || tokenizeKeyword("^values$", TokenType.VALUES)
                    || tokenizeKeyword("^where$", TokenType.WHERE)
                    || tokenizeKeyword("^required$", TokenType.REQUIRED)
                    || tokenizeKeyword("^nullable$", TokenType.NULLABLE)
                    || tokenizeKeyword("^default$", TokenType.DEFAULT)
                    || tokenizeKeyword("^and$", TokenType.AND)
                    || tokenizeKeyword("^or$", TokenType.OR)
                    || tokenizeKeyword("^asc$", TokenType.ASCENDING)
                    || tokenizeKeyword("^desc$", TokenType.DESCENDING)

                    || tokenizeKeyword("^count$", TokenType.COUNT)
                    || tokenizeKeyword("^average$", TokenType.AVERAGE)
                    || tokenizeKeyword("^sum$", TokenType.SUM)

                    || tokenizeKeyword("^int$", TokenType.INT_TYPE)
                    || tokenizeKeyword("^float$", TokenType.FLOAT_TYPE)
                    || tokenizeKeyword("^string$", TokenType.STRING_TYPE)
                    || tokenizeKeyword("^bool$", TokenType.BOOL_TYPE)

                    || tokenizeAll() // Must be above MULTIPLY_OP tokenizer.

                    || tokenizeSpecialChars("+", TokenType.ADD_OP)
                    || tokenizeMinusOp()
                    || tokenizeSpecialChars("*", TokenType.MULTIPLY_OP)
                    || tokenizeSpecialChars("/", TokenType.DIVIDE_OP)
                    || tokenizeComparisonOps()

                    || tokenizeSpecialChars("(", TokenType.OPEN_PAREN)
                    || tokenizeSpecialChars(")", TokenType.CLOSE_PAREN)
                    || tokenizeSpecialChars(";", TokenType.SEMICOLON)
                    || tokenizeSpecialChars(",", TokenType.COMMA)

                    || tokenizeKeyword("^(true|false)$", TokenType.BOOL_LITERAL)
                    || tokenizeKeyword("^null$", TokenType.NULL_LITERAL)
                    || tokenizeNumber("^-?\\d+$", TokenType.INT_LITERAL)
                    || tokenizeNumber("^-?\\d*\\.\\d+|\\d+\\.\\d*$", TokenType.FLOAT_LITERAL)
                    || tokenizeStringLiteral()

                    || tokenizeIdentifier() // Must be below all the keyword tokenizers.
            ) {
                queryBufferStart = queryBufferEnd;
            }

            queryBufferEnd++;
        }

        if (queryBufferEnd - queryBufferStart > 1) {
            // Query buffer is not empty, meaning there are tokens we were unable to parse.
            final var queryBuffer = query.substring(queryBufferStart, queryBufferEnd - 1);
            throw new IllegalArgumentException("Syntax error: Cannot parse - \"" + queryBuffer + "\".");
        }

        return tokens;
    }

    /**
     * White space includes blanks, new lines and tabs.
     */
    private boolean ignoreWhiteSpace() {
        final var queryBuffer = query.substring(queryBufferStart, queryBufferEnd);
        final var pattern = Pattern.compile("^\\s+$");
        final var matcher = pattern.matcher(queryBuffer);
        return matcher.find();
    }

    /**
     * Comments start with two hyphens "--"" and ends with a line terminator e.g. new line.
     */
    private boolean ignoreComment() {
        final var queryBuffer = query.substring(queryBufferStart, queryBufferEnd);
        final var pattern = Pattern.compile("^--.*\\R$");
        final var matcher = pattern.matcher(queryBuffer);
        return matcher.find();
    }

    private boolean isWord(String str) {
        final var pattern = Pattern.compile("^[a-zA-Z0-9_]$");
        final var matcher = pattern.matcher(str);
        return matcher.find();
    }

    private boolean charRightOfQueryBufferIsWord() {
        return queryBufferEnd < query.length() && isWord(Character.toString(query.charAt(queryBufferEnd)));
    }

    private boolean isNumber(Token tok) {
        return NUMERIC_LITERALS.contains(tok.getTokenType());
    }

    private boolean isIdentifier(Token tok) {
        return tok.getTokenType() == TokenType.IDENTIFIER;
    }

    private boolean tokenizeKeyword(String regex, TokenType tokenType) {
        final var queryBuffer = query.substring(queryBufferStart, queryBufferEnd);
        final var pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        final var matcher = pattern.matcher(queryBuffer);
        if (!matcher.find()) {
            return false;
        }

        if (charRightOfQueryBufferIsWord()) {
            return false;
        }

        tokens.add(new Token(tokenType, queryBuffer));
        return true;
    }

    private boolean latestTokenIsNumber() {
        return tokens.size() > 0 && isNumber(tokens.get(tokens.size() - 1));
    }

    private boolean latestTokenIsIdentifier() {
        return tokens.size() > 0 && isIdentifier(tokens.get(tokens.size() - 1));
    }

    private boolean tokenizeAll() {
        final var queryBuffer = query.substring(queryBufferStart, queryBufferEnd);
        final var pattern = "*";
        if (!pattern.equals(queryBuffer)) {
            return false;
        }

        // Check to prevent conflating ALL token with MULTIPLY_OP since both are "*".
        if (latestTokenIsNumber() || latestTokenIsIdentifier()) {
            return false;
        }
        
        tokens.add(new Token(TokenType.ALL, queryBuffer));
        return true;
    }

    private boolean tokenizeIdentifier() {
        final var queryBuffer = query.substring(queryBufferStart, queryBufferEnd);
        final var pattern = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
        final var matcher = pattern.matcher(queryBuffer);
        if (!matcher.find()) {
            return false;
        }

        if (charRightOfQueryBufferIsWord()) {
            return false;
        }
        
        tokens.add(new Token(TokenType.IDENTIFIER, queryBuffer));
        return true;
    }

    private boolean tokenizeSpecialChars(String pattern, TokenType tokenType) {
        final var queryBuffer = query.substring(queryBufferStart, queryBufferEnd);
        if (!pattern.equals(queryBuffer)) {
            return false;
        }

        tokens.add(new Token(tokenType, queryBuffer));
        return true;
    }

    private boolean tokenizeMinusOp() {
        final var queryBuffer = query.substring(queryBufferStart, queryBufferEnd);
        if (!"-".equals(queryBuffer)) {
            return false;
        }

        // Check to prevent conflating a negative int or float literal with the subtract op.
        if (latestTokenIsNumber() || latestTokenIsIdentifier()) {
            tokens.add(new Token(TokenType.SUBTRACT_OP, queryBuffer));
            return true;
        }

        return false;
    }

    private boolean tokenizeComparisonOps() {
        final var queryBuffer = query.substring(queryBufferStart, queryBufferEnd);
        if (!COMPARISON_OPS.containsKey(queryBuffer)) {
            return false;
        }

        final var charRightOfQueryBufferIsEqualChar = queryBufferEnd < query.length() && query.charAt(queryBufferEnd) == '=';
        if ((">".equals(queryBuffer) || "<".equals(queryBuffer) || "!".equals(queryBuffer))
                && charRightOfQueryBufferIsEqualChar) {
            return false;
        }

        tokens.add(new Token(COMPARISON_OPS.get(queryBuffer), queryBuffer));
        return true;
    }

    private boolean charRightOfQueryBufferIsNumber() {
        return queryBufferEnd < query.length()
                && Character.toString(query.charAt(queryBufferEnd)).matches("^\\d$");
    }

    private boolean charRightOfQueryBufferIsPointChar() {
        return queryBufferEnd < query.length() && query.charAt(queryBufferEnd) == '.';
    }

    private boolean tokenizeNumber(String regex, TokenType tokenType) {
        final var queryBuffer = query.substring(queryBufferStart, queryBufferEnd);
        final var pattern = Pattern.compile(regex);
        final var matcher = pattern.matcher(queryBuffer);
        if (!matcher.find()) {
            return false;
        }

        if (charRightOfQueryBufferIsNumber() || charRightOfQueryBufferIsPointChar()) {
            return false;
        }

        tokens.add(new Token(tokenType, queryBuffer));
        return true;
    }

    private boolean tokenizeStringLiteral() {
        final var queryBuffer = query.substring(queryBufferStart, queryBufferEnd);
        final var pattern = Pattern.compile("^\".*\"$");
        final var matcher = pattern.matcher(queryBuffer);
        if (!matcher.find()) {
            return false;
        }

        // If there's an escape character before a quote char, then it's part of the string literal.
        final var penultimateCharInQueryBufferIsBackSlash = "\\".equals(queryBuffer.substring(queryBuffer.length() - 2, queryBuffer.length() - 1));
        if (penultimateCharInQueryBufferIsBackSlash) {
            return false;
        }

        tokens.add(new Token(TokenType.STRING_LITERAL, queryBuffer));
        return true;
    }
}
