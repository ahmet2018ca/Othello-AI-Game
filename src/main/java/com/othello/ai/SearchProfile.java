package com.othello.ai;

public record SearchProfile(
        int maxDepth,
        int timeLimitMillis,
        int exactSolveEmptySquares,
        int randomnessWindow,
        int candidatePool,
        boolean iterativeDeepening
) {
    public SearchProfile {
        if (maxDepth < 1) {
            throw new IllegalArgumentException("maxDepth must be positive");
        }
        if (timeLimitMillis < 1) {
            throw new IllegalArgumentException("timeLimitMillis must be positive");
        }
        if (exactSolveEmptySquares < 0 || exactSolveEmptySquares > 64) {
            throw new IllegalArgumentException("exactSolveEmptySquares must be between 0 and 64");
        }
        if (randomnessWindow < 0) {
            throw new IllegalArgumentException("randomnessWindow cannot be negative");
        }
        if (candidatePool < 1) {
            throw new IllegalArgumentException("candidatePool must be positive");
        }
    }
}

