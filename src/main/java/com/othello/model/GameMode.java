package com.othello.model;

public enum GameMode {
    HUMAN_VS_HUMAN("Human vs Human"),
    HUMAN_VS_AI("Human vs AI");

    private final String label;

    GameMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}

