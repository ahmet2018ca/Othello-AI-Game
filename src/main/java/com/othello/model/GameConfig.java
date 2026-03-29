package com.othello.model;

public record GameConfig(GameMode mode, Difficulty difficulty, Disc humanColor) {
    public static GameConfig humanVsHuman() {
        return new GameConfig(GameMode.HUMAN_VS_HUMAN, Difficulty.HARD, Disc.BLACK);
    }

    public static GameConfig humanVsAi(Difficulty difficulty, Disc humanColor) {
        if (difficulty == null) {
            throw new IllegalArgumentException("difficulty is required for AI games");
        }
        if (humanColor == null) {
            throw new IllegalArgumentException("humanColor is required for AI games");
        }
        return new GameConfig(GameMode.HUMAN_VS_AI, difficulty, humanColor);
    }

    public boolean isHumanControlled(Disc disc) {
        return switch (mode) {
            case HUMAN_VS_HUMAN -> true;
            case HUMAN_VS_AI -> disc == humanColor;
        };
    }
}
