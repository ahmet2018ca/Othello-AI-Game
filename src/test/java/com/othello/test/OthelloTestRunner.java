package com.othello.test;

import com.othello.ai.OthelloAi;
import com.othello.engine.BoardState;
import com.othello.model.Difficulty;
import com.othello.model.Disc;

public final class OthelloTestRunner {
    private OthelloTestRunner() {
    }

    public static void main(String[] args) {
        testInitialPosition();
        testOpeningFlip();
        testAiAlwaysFindsLegalMove();
        testPassScenario();
        System.out.println("All Othello smoke tests passed.");
    }

    private static void testInitialPosition() {
        var state = BoardState.initial();
        assertEquals(4, state.legalMovesList().size(), "Initial position should offer 4 legal moves");
        assertEquals(2, state.discCount(Disc.BLACK), "Black should start with 2 discs");
        assertEquals(2, state.discCount(Disc.WHITE), "White should start with 2 discs");
    }

    private static void testOpeningFlip() {
        var state = BoardState.initial();
        var move = state.findLegalMove(19);
        if (move == null) {
            throw new AssertionError("Expected D3 to be legal in the starting position");
        }

        var next = state.apply(move);
        assertEquals(4, next.discCount(Disc.BLACK), "Black should have 4 discs after D3");
        assertEquals(1, next.discCount(Disc.WHITE), "White should have 1 disc after D3");
    }

    private static void testAiAlwaysFindsLegalMove() {
        var ai = new OthelloAi();
        var state = BoardState.initial();
        for (var difficulty : Difficulty.values()) {
            var outcome = ai.chooseMove(state, difficulty);
            if (state.findLegalMove(outcome.move().index()) == null) {
                throw new AssertionError("AI returned an illegal move for " + difficulty.label());
            }
        }
    }

    private static void testPassScenario() {
        var state = BoardState.fromRows(
                Disc.BLACK,
                "WWWWWWW.",
                "WWWWB...",
                "WBBB....",
                "..BBB...",
                "...BBB..",
                "........",
                "........",
                "........"
        );

        if (state.hasAnyLegalMove(Disc.BLACK)) {
            throw new AssertionError("Black should be forced to pass in the test position");
        }
        if (!state.hasAnyLegalMove(Disc.WHITE)) {
            throw new AssertionError("White should still have a move in the test position");
        }
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError("%s. Expected %d but found %d".formatted(message, expected, actual));
        }
    }
}
