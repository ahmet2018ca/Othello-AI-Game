package com.othello.model;

import java.util.Optional;

public record GameResult(int blackCount, int whiteCount) {
    public Optional<Disc> winner() {
        if (blackCount == whiteCount) {
            return Optional.empty();
        }
        return Optional.of(blackCount > whiteCount ? Disc.BLACK : Disc.WHITE);
    }

    public boolean isDraw() {
        return blackCount == whiteCount;
    }

    public String summary() {
        return winner()
                .map(disc -> "%s wins %d-%d".formatted(disc.label(), blackCount, whiteCount))
                .orElse("Draw game at %d-%d".formatted(blackCount, whiteCount));
    }
}
