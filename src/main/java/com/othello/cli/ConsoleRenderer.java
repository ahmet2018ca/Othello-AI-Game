package com.othello.cli;

import com.othello.ai.SearchOutcome;
import com.othello.engine.BoardState;
import com.othello.model.Disc;
import com.othello.model.Move;

import java.util.List;

public final class ConsoleRenderer {
    public void printBanner() {
        System.out.println();
        System.out.println("==============================================================");
        System.out.println("              Game-Theoretic Othello AI Engine");
        System.out.println("==============================================================");
        System.out.println("Bitboards. Minimax. Alpha-beta pruning. Endgame solving.");
        System.out.println();
    }

    public void printBoard(BoardState state, SearchOutcome analysis, Move hintedMove) {
        long legalMoves = state.legalMoves();
        System.out.println();
        System.out.printf(
                "Ply %d | To Move: %s | Black %d  White %d%n",
                state.ply() + 1,
                state.sideToMove().label(),
                state.discCount(Disc.BLACK),
                state.discCount(Disc.WHITE)
        );
        System.out.printf("Legal Moves: %s%n", String.join(", ", state.legalMoveNotations()));
        if (analysis != null) {
            System.out.printf(
                    "Latest Search: %s | score %+d | depth %d | %,d nodes | %d ms | pv %s%n",
                    analysis.move().notation(),
                    analysis.score(),
                    analysis.depth(),
                    analysis.nodes(),
                    analysis.elapsedMillis(),
                    analysis.principalVariationText()
            );
        }

        System.out.println("    A B C D E F G H");
        for (int row = 0; row < 8; row++) {
            System.out.printf(" %d  ", row + 1);
            for (int col = 0; col < 8; col++) {
                int index = row * 8 + col;
                long bit = 1L << index;
                char cell;
                var occupant = state.occupantAt(index);
                if (occupant == Disc.BLACK) {
                    cell = 'B';
                } else if (occupant == Disc.WHITE) {
                    cell = 'W';
                } else if (hintedMove != null && hintedMove.index() == index) {
                    cell = '?';
                } else if ((legalMoves & bit) != 0L) {
                    cell = '*';
                } else {
                    cell = '.';
                }
                System.out.print(cell);
                System.out.print(' ');
            }
            System.out.printf(" %d%n", row + 1);
        }
        System.out.println("    A B C D E F G H");
        System.out.println();
        System.out.println("Commands: <move>  hint  moves  undo  help  quit");
        if (hintedMove != null) {
            System.out.printf("Hint marker: %s%n", hintedMove.notation());
        }
    }

    public void printMenu(List<String> options) {
        System.out.println("Main Menu");
        for (int i = 0; i < options.size(); i++) {
            System.out.printf(" %d. %s%n", i + 1, options.get(i));
        }
    }

    public void printRules() {
        System.out.println();
        System.out.println("Othello Rules and Controls");
        System.out.println("Place a disc so that at least one straight line of enemy discs");
        System.out.println("is bracketed between the disc you place and one of your existing discs.");
        System.out.println("All bracketed enemy discs flip to your color immediately.");
        System.out.println("If a player has no legal moves, that player passes automatically.");
        System.out.println("The game ends when neither side can move; most discs wins.");
        System.out.println();
        System.out.println("CLI Notes");
        System.out.println("Enter coordinates like D3 or F6.");
        System.out.println("Use hint to ask the engine for a recommendation.");
        System.out.println("Use undo to rewind one ply.");
        System.out.println("Use quit to return to the main menu.");
        System.out.println();
    }
}

