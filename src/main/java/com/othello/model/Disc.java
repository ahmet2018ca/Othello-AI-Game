package com.othello.model;

public enum Disc {
    BLACK('B'),
    WHITE('W');

    private final char symbol;

    Disc(char symbol) {
        this.symbol = symbol;
    }

    public Disc opponent() {
        return switch (this) {
            case BLACK -> WHITE;
            case WHITE -> BLACK;
        };
    }

    public char symbol() {
        return symbol;
    }

    public String label() {
        return switch (this) {
            case BLACK -> "Black";
            case WHITE -> "White";
        };
    }
}

