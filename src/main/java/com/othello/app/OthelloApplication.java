package com.othello.app;

import com.othello.cli.ConsoleGameController;

public final class OthelloApplication {
    private OthelloApplication() {
    }

    public static void main(String[] args) {
        new ConsoleGameController().run();
    }
}

