package com.othello.cli;

import com.othello.ai.OthelloAi;
import com.othello.ai.SearchOutcome;
import com.othello.engine.BoardState;
import com.othello.model.BoardNotation;
import com.othello.model.Difficulty;
import com.othello.model.Disc;
import com.othello.model.GameConfig;
import com.othello.model.Move;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public final class ConsoleGameController {
    private final Scanner scanner = new Scanner(System.in);
    private final ConsoleRenderer renderer = new ConsoleRenderer();
    private final OthelloAi ai = new OthelloAi();

    public void run() {
        renderer.printBanner();

        boolean running = true;
        while (running) {
            renderer.printMenu(List.of(
                    "Human vs Human",
                    "Human vs AI",
                    "Rules and Controls",
                    "Exit"
            ));

            switch (readMenuChoice(4)) {
                case 1 -> playGame(GameConfig.humanVsHuman());
                case 2 -> playGame(GameConfig.humanVsAi(promptDifficulty(), promptHumanColor()));
                case 3 -> renderer.printRules();
                case 4 -> running = false;
                default -> {
                }
            }
        }
    }

    private void playGame(GameConfig config) {
        var history = new ArrayDeque<BoardState>();
        var state = BoardState.initial();
        SearchOutcome analysis = null;
        Move hintedMove = null;

        while (!state.isTerminal()) {
            renderer.printBoard(state, analysis, hintedMove);
            hintedMove = null;

            if (!state.hasAnyLegalMove(state.sideToMove())) {
                System.out.printf("%s has no legal moves and passes.%n", state.sideToMove().label());
                history.push(state);
                state = state.pass();
                continue;
            }

            if (config.isHumanControlled(state.sideToMove())) {
                var action = promptHumanAction(state, config);
                switch (action.kind()) {
                    case MOVE -> {
                        history.push(state);
                        state = state.apply(action.move());
                        analysis = null;
                    }
                    case UNDO -> {
                        if (history.isEmpty()) {
                            System.out.println("Nothing to undo yet.");
                        } else {
                            state = history.pop();
                            analysis = null;
                            hintedMove = null;
                        }
                    }
                    case HINT -> {
                        var hintDifficulty = config.mode() == com.othello.model.GameMode.HUMAN_VS_AI
                                ? config.difficulty()
                                : Difficulty.HARD;
                        System.out.printf("Computing %s hint...%n", hintDifficulty.label());
                        analysis = ai.chooseMove(state, hintDifficulty);
                        hintedMove = analysis.move();
                    }
                    case QUIT_TO_MENU -> {
                        return;
                    }
                }
            } else {
                System.out.printf("%s AI (%s) is thinking...%n", state.sideToMove().label(), config.difficulty().label());
                analysis = ai.chooseMove(state, config.difficulty());
                history.push(state);
                state = state.apply(analysis.move());
                System.out.printf(
                        "%s AI plays %s | score %+d | depth %d | %,d nodes | %d ms%n",
                        state.sideToMove().opponent().label(),
                        analysis.move().notation(),
                        analysis.score(),
                        analysis.depth(),
                        analysis.nodes(),
                        analysis.elapsedMillis()
                );
            }
        }

        renderer.printBoard(state, analysis, null);
        System.out.printf("Game Over: %s%n", state.result().summary());
    }

    private TurnAction promptHumanAction(BoardState state, GameConfig config) {
        while (true) {
            System.out.printf("%s to move > ", state.sideToMove().label());
            var input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            var token = input.toLowerCase(Locale.ROOT);
            switch (token) {
                case "hint" -> {
                    return TurnAction.hint();
                }
                case "moves" -> {
                    System.out.printf("Legal moves: %s%n", String.join(", ", state.legalMoveNotations()));
                }
                case "undo" -> {
                    return TurnAction.undo();
                }
                case "quit", "menu", "exit" -> {
                    return TurnAction.quitToMenu();
                }
                case "help" -> renderer.printRules();
                default -> {
                    var parsedIndex = BoardNotation.parseSquare(input);
                    if (parsedIndex >= 0) {
                        var move = state.findLegalMove(parsedIndex);
                        if (move != null) {
                            return TurnAction.move(move);
                        }
                    }
                    System.out.println("That is not a legal move right now. Try a coordinate like D3.");
                }
            }
        }
    }

    private Difficulty promptDifficulty() {
        System.out.println();
        System.out.println("Select AI Difficulty");
        var values = Difficulty.values();
        for (int i = 0; i < values.length; i++) {
            System.out.printf(" %d. %s - %s%n", i + 1, values[i].label(), values[i].description());
        }
        return values[readMenuChoice(values.length) - 1];
    }

    private Disc promptHumanColor() {
        System.out.println();
        System.out.println("Choose your color");
        System.out.println(" 1. Black (moves first)");
        System.out.println(" 2. White");
        return readMenuChoice(2) == 1 ? Disc.BLACK : Disc.WHITE;
    }

    private int readMenuChoice(int max) {
        while (true) {
            System.out.print("> ");
            var input = scanner.nextLine().trim();
            try {
                var value = Integer.parseInt(input);
                if (value >= 1 && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.printf("Enter a number between 1 and %d.%n", max);
        }
    }

    private record TurnAction(ActionKind kind, Move move) {
        private static TurnAction move(Move move) {
            return new TurnAction(ActionKind.MOVE, move);
        }

        private static TurnAction undo() {
            return new TurnAction(ActionKind.UNDO, null);
        }

        private static TurnAction hint() {
            return new TurnAction(ActionKind.HINT, null);
        }

        private static TurnAction quitToMenu() {
            return new TurnAction(ActionKind.QUIT_TO_MENU, null);
        }
    }

    private enum ActionKind {
        MOVE,
        UNDO,
        HINT,
        QUIT_TO_MENU
    }
}

