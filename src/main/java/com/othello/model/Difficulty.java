package com.othello.model;

import com.othello.ai.SearchProfile;

public enum Difficulty {
    EASY(
            "Easy",
            "Greedy tactical search with a splash of randomness.",
            new SearchProfile(3, 125, 8, 225, 3, true)
    ),
    MEDIUM(
            "Medium",
            "Balanced positional search with alpha-beta pruning.",
            new SearchProfile(5, 400, 10, 90, 2, true)
    ),
    HARD(
            "Hard",
            "Deep iterative search with killer ordering and endgame solving.",
            new SearchProfile(8, 1_250, 12, 0, 1, true)
    ),
    GODLIKE(
            "Godlike",
            "Tournament-grade search that pushes exact late-game lines aggressively.",
            new SearchProfile(12, 3_250, 16, 0, 1, true)
    );

    private final String label;
    private final String description;
    private final SearchProfile profile;

    Difficulty(String label, String description, SearchProfile profile) {
        this.label = label;
        this.description = description;
        this.profile = profile;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public SearchProfile profile() {
        return profile;
    }
}

