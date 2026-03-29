package com.othello.ai;

import com.othello.model.Move;

import java.util.List;
import java.util.stream.Collectors;

public record SearchOutcome(
        Move move,
        int score,
        int depth,
        long nodes,
        long elapsedMillis,
        List<Move> principalVariation
) {
    public String principalVariationText() {
        return principalVariation.stream()
                .map(Move::notation)
                .collect(Collectors.joining(" -> "));
    }
}

