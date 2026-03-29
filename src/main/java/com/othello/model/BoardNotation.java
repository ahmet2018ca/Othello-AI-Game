package com.othello.model;

public final class BoardNotation {
    private BoardNotation() {
    }

    public static String squareName(int index) {
        if (index < 0 || index >= 64) {
            throw new IllegalArgumentException("index must be between 0 and 63");
        }
        var file = (char) ('A' + (index % 8));
        var rank = 1 + (index / 8);
        return "%c%d".formatted(file, rank);
    }

    public static int parseSquare(String token) {
        var trimmed = token.trim().toUpperCase();
        if (trimmed.length() != 2) {
            return -1;
        }

        var file = trimmed.charAt(0);
        var rank = trimmed.charAt(1);
        if (file < 'A' || file > 'H' || rank < '1' || rank > '8') {
            return -1;
        }
        return (rank - '1') * 8 + (file - 'A');
    }
}

