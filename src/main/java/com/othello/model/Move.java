package com.othello.model;

public record Move(int index, long bit, long flipped) {
    public Move {
        if (index < 0 || index >= 64) {
            throw new IllegalArgumentException("index must be between 0 and 63");
        }
        if (bit == 0L) {
            throw new IllegalArgumentException("bit cannot be zero");
        }
    }

    public static Move of(int index, long flipped) {
        return new Move(index, 1L << index, flipped);
    }

    public int row() {
        return index / 8;
    }

    public int col() {
        return index % 8;
    }

    public int flipCount() {
        return Long.bitCount(flipped);
    }

    public String notation() {
        return BoardNotation.squareName(index);
    }
}

