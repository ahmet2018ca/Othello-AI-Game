package com.othello.ai;

import com.othello.engine.BoardState;
import com.othello.model.Disc;

import java.util.Arrays;

public final class TranspositionTable {
    public static final byte EXACT = 0;
    public static final byte LOWER_BOUND = 1;
    public static final byte UPPER_BOUND = 2;

    private final int mask;
    private final long[] blackKeys;
    private final long[] whiteKeys;
    private final byte[] sideKeys;
    private final short[] depths;
    private final int[] scores;
    private final byte[] flags;
    private final int[] bestMoves;
    private final int[] generations;
    private int currentGeneration = 1;

    public TranspositionTable(int requestedSize) {
        var capacity = 1;
        while (capacity < requestedSize) {
            capacity <<= 1;
        }
        mask = capacity - 1;
        blackKeys = new long[capacity];
        whiteKeys = new long[capacity];
        sideKeys = new byte[capacity];
        depths = new short[capacity];
        scores = new int[capacity];
        flags = new byte[capacity];
        bestMoves = new int[capacity];
        generations = new int[capacity];
        Arrays.fill(bestMoves, -1);
    }

    public void nextGeneration() {
        currentGeneration++;
        if (currentGeneration == Integer.MAX_VALUE) {
            Arrays.fill(generations, 0);
            currentGeneration = 1;
        }
    }

    public Entry probe(BoardState state) {
        var slot = slot(state);
        if (generations[slot] != currentGeneration) {
            return null;
        }
        if (blackKeys[slot] != state.blackDiscs()
                || whiteKeys[slot] != state.whiteDiscs()
                || sideKeys[slot] != sideKey(state.sideToMove())) {
            return null;
        }
        return new Entry(depths[slot], scores[slot], flags[slot], bestMoves[slot]);
    }

    public void store(BoardState state, int depth, int score, byte flag, int bestMove) {
        var slot = slot(state);
        if (generations[slot] == currentGeneration
                && blackKeys[slot] == state.blackDiscs()
                && whiteKeys[slot] == state.whiteDiscs()
                && sideKeys[slot] == sideKey(state.sideToMove())
                && depth < depths[slot]) {
            return;
        }

        blackKeys[slot] = state.blackDiscs();
        whiteKeys[slot] = state.whiteDiscs();
        sideKeys[slot] = sideKey(state.sideToMove());
        depths[slot] = (short) depth;
        scores[slot] = score;
        flags[slot] = flag;
        bestMoves[slot] = bestMove;
        generations[slot] = currentGeneration;
    }

    private int slot(BoardState state) {
        long mixed = mix(state.blackDiscs(), state.whiteDiscs(), state.sideToMove());
        return (int) mixed & mask;
    }

    private static long mix(long black, long white, Disc sideToMove) {
        long hash = black * 0x9E37_79B9_7F4A_7C15L;
        hash ^= Long.rotateLeft(white, 29) * 0xC2B2_AE3D_27D4_EB4FL;
        hash ^= sideToMove == Disc.BLACK
                ? 0x94D0_49BB_1331_11EBL
                : 0xD6E8_FEB8_6659_FD93L;
        hash ^= hash >>> 33;
        hash *= 0xFF51_AFD7_ED55_8CCDL;
        hash ^= hash >>> 33;
        hash *= 0xC4CE_B9FE_1A85_EC53L;
        hash ^= hash >>> 33;
        return hash;
    }

    private static byte sideKey(Disc disc) {
        return (byte) disc.ordinal();
    }

    public record Entry(int depth, int score, byte flag, int bestMove) {
    }
}
