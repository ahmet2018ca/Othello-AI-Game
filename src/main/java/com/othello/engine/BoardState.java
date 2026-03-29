package com.othello.engine;

import com.othello.model.BoardNotation;
import com.othello.model.Disc;
import com.othello.model.GameResult;
import com.othello.model.Move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.LongUnaryOperator;

public final class BoardState {
    private static final long A_FILE = 0x0101_0101_0101_0101L;
    private static final long H_FILE = 0x8080_8080_8080_8080L;
    private static final long NOT_A_FILE = ~A_FILE;
    private static final long NOT_H_FILE = ~H_FILE;

    private final long blackDiscs;
    private final long whiteDiscs;
    private final Disc sideToMove;
    private final int consecutivePasses;
    private final int ply;

    public BoardState(long blackDiscs, long whiteDiscs, Disc sideToMove, int consecutivePasses, int ply) {
        if ((blackDiscs & whiteDiscs) != 0L) {
            throw new IllegalArgumentException("Black and white discs cannot overlap");
        }
        if (sideToMove == null) {
            throw new IllegalArgumentException("sideToMove cannot be null");
        }
        if (consecutivePasses < 0 || consecutivePasses > 2) {
            throw new IllegalArgumentException("consecutivePasses must be between 0 and 2");
        }
        if (ply < 0) {
            throw new IllegalArgumentException("ply cannot be negative");
        }
        this.blackDiscs = blackDiscs;
        this.whiteDiscs = whiteDiscs;
        this.sideToMove = sideToMove;
        this.consecutivePasses = consecutivePasses;
        this.ply = ply;
    }

    public static BoardState initial() {
        var black = bit(28) | bit(35);
        var white = bit(27) | bit(36);
        return new BoardState(black, white, Disc.BLACK, 0, 0);
    }

    public static BoardState fromRows(Disc sideToMove, String... rows) {
        if (rows.length != 8) {
            throw new IllegalArgumentException("Exactly 8 rows are required");
        }

        long black = 0L;
        long white = 0L;
        for (int row = 0; row < 8; row++) {
            if (rows[row].length() != 8) {
                throw new IllegalArgumentException("Each row must contain 8 characters");
            }
            for (int col = 0; col < 8; col++) {
                var cell = Character.toUpperCase(rows[row].charAt(col));
                var index = row * 8 + col;
                switch (cell) {
                    case 'B' -> black |= bit(index);
                    case 'W' -> white |= bit(index);
                    case '.', '-', '_' -> {
                    }
                    default -> throw new IllegalArgumentException("Unsupported board character: " + cell);
                }
            }
        }

        var occupied = Long.bitCount(black | white);
        return new BoardState(black, white, sideToMove, 0, Math.max(0, occupied - 4));
    }

    public long blackDiscs() {
        return blackDiscs;
    }

    public long whiteDiscs() {
        return whiteDiscs;
    }

    public Disc sideToMove() {
        return sideToMove;
    }

    public int consecutivePasses() {
        return consecutivePasses;
    }

    public int ply() {
        return ply;
    }

    public long bits(Disc disc) {
        return disc == Disc.BLACK ? blackDiscs : whiteDiscs;
    }

    public int discCount(Disc disc) {
        return Long.bitCount(bits(disc));
    }

    public int occupiedCount() {
        return Long.bitCount(blackDiscs | whiteDiscs);
    }

    public int emptyCount() {
        return 64 - occupiedCount();
    }

    public long emptySquares() {
        return ~(blackDiscs | whiteDiscs);
    }

    public Disc occupantAt(int index) {
        var mask = bit(index);
        if ((blackDiscs & mask) != 0L) {
            return Disc.BLACK;
        }
        if ((whiteDiscs & mask) != 0L) {
            return Disc.WHITE;
        }
        return null;
    }

    public long legalMoves() {
        return legalMoves(sideToMove);
    }

    public long legalMoves(Disc disc) {
        return computeLegalMoves(bits(disc), bits(disc.opponent()));
    }

    public boolean hasAnyLegalMove(Disc disc) {
        return legalMoves(disc) != 0L;
    }

    public List<Move> legalMovesList() {
        return legalMovesList(sideToMove);
    }

    public List<Move> legalMovesList(Disc disc) {
        var player = bits(disc);
        var opponent = bits(disc.opponent());
        long mask = computeLegalMoves(player, opponent);
        var moves = new ArrayList<Move>(Math.max(1, Long.bitCount(mask)));
        while (mask != 0L) {
            var bit = Long.lowestOneBit(mask);
            var index = Long.numberOfTrailingZeros(bit);
            var flipped = computeFlips(bit, player, opponent);
            moves.add(new Move(index, bit, flipped));
            mask ^= bit;
        }
        return moves;
    }

    public Move findLegalMove(int index) {
        if (index < 0 || index >= 64) {
            return null;
        }
        for (var move : legalMovesList()) {
            if (move.index() == index) {
                return move;
            }
        }
        return null;
    }

    public BoardState apply(Move move) {
        if (move == null) {
            throw new IllegalArgumentException("move cannot be null");
        }

        var player = bits(sideToMove);
        var opponent = bits(sideToMove.opponent());
        var legalMask = computeLegalMoves(player, opponent);
        if ((legalMask & move.bit()) == 0L) {
            throw new IllegalArgumentException("Illegal move: " + move.notation());
        }

        var flipped = move.flipped() == 0L
                ? computeFlips(move.bit(), player, opponent)
                : move.flipped();
        var newPlayer = player | move.bit() | flipped;
        var newOpponent = opponent & ~flipped;

        return sideToMove == Disc.BLACK
                ? new BoardState(newPlayer, newOpponent, Disc.WHITE, 0, ply + 1)
                : new BoardState(newOpponent, newPlayer, Disc.BLACK, 0, ply + 1);
    }

    public BoardState pass() {
        if (legalMoves() != 0L) {
            throw new IllegalStateException("Cannot pass while a legal move exists");
        }
        return new BoardState(blackDiscs, whiteDiscs, sideToMove.opponent(), consecutivePasses + 1, ply);
    }

    public boolean isTerminal() {
        return consecutivePasses >= 2
                || occupiedCount() == 64
                || (legalMoves(Disc.BLACK) == 0L && legalMoves(Disc.WHITE) == 0L);
    }

    public GameResult result() {
        return new GameResult(discCount(Disc.BLACK), discCount(Disc.WHITE));
    }

    public List<String> legalMoveNotations() {
        var notations = new ArrayList<String>();
        for (var move : legalMovesList()) {
            notations.add(move.notation());
        }
        return notations;
    }

    public static long adjacent(long bitboard) {
        return north(bitboard)
                | south(bitboard)
                | east(bitboard)
                | west(bitboard)
                | northEast(bitboard)
                | northWest(bitboard)
                | southEast(bitboard)
                | southWest(bitboard);
    }

    public static String squareName(int index) {
        return BoardNotation.squareName(index);
    }

    private static long computeLegalMoves(long player, long opponent) {
        var empty = ~(player | opponent);
        long moves = 0L;
        moves |= scanDirection(player, opponent, empty, BoardState::north);
        moves |= scanDirection(player, opponent, empty, BoardState::south);
        moves |= scanDirection(player, opponent, empty, BoardState::east);
        moves |= scanDirection(player, opponent, empty, BoardState::west);
        moves |= scanDirection(player, opponent, empty, BoardState::northEast);
        moves |= scanDirection(player, opponent, empty, BoardState::northWest);
        moves |= scanDirection(player, opponent, empty, BoardState::southEast);
        moves |= scanDirection(player, opponent, empty, BoardState::southWest);
        return moves & empty;
    }

    private static long scanDirection(long player, long opponent, long empty, LongUnaryOperator shift) {
        long flood = shift.applyAsLong(player) & opponent;
        for (int i = 0; i < 5; i++) {
            flood |= shift.applyAsLong(flood) & opponent;
        }
        return shift.applyAsLong(flood) & empty;
    }

    private static long computeFlips(long moveBit, long player, long opponent) {
        long flips = 0L;
        flips |= captureDirection(moveBit, player, opponent, BoardState::north);
        flips |= captureDirection(moveBit, player, opponent, BoardState::south);
        flips |= captureDirection(moveBit, player, opponent, BoardState::east);
        flips |= captureDirection(moveBit, player, opponent, BoardState::west);
        flips |= captureDirection(moveBit, player, opponent, BoardState::northEast);
        flips |= captureDirection(moveBit, player, opponent, BoardState::northWest);
        flips |= captureDirection(moveBit, player, opponent, BoardState::southEast);
        flips |= captureDirection(moveBit, player, opponent, BoardState::southWest);
        return flips;
    }

    private static long captureDirection(long moveBit, long player, long opponent, LongUnaryOperator shift) {
        long seen = 0L;
        long cursor = shift.applyAsLong(moveBit) & opponent;
        while (cursor != 0L) {
            seen |= cursor;
            var next = shift.applyAsLong(cursor);
            if ((next & player) != 0L) {
                return seen;
            }
            cursor = next & opponent;
        }
        return 0L;
    }

    private static long north(long value) {
        return value >>> 8;
    }

    private static long south(long value) {
        return value << 8;
    }

    private static long east(long value) {
        return (value << 1) & NOT_A_FILE;
    }

    private static long west(long value) {
        return (value >>> 1) & NOT_H_FILE;
    }

    private static long northEast(long value) {
        return (value >>> 7) & NOT_A_FILE;
    }

    private static long northWest(long value) {
        return (value >>> 9) & NOT_H_FILE;
    }

    private static long southEast(long value) {
        return (value << 9) & NOT_A_FILE;
    }

    private static long southWest(long value) {
        return (value << 7) & NOT_H_FILE;
    }

    private static long bit(int index) {
        return 1L << index;
    }

    @Override
    public String toString() {
        return "BoardState{black=%d, white=%d, side=%s, legal=%s}".formatted(
                discCount(Disc.BLACK),
                discCount(Disc.WHITE),
                sideToMove,
                Arrays.toString(legalMoveNotations().toArray())
        );
    }
}
