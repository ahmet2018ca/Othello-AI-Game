package com.othello.ai;

import com.othello.engine.BoardState;

public final class Evaluator {
    private static final long CORNERS = bit(0) | bit(7) | bit(56) | bit(63);
    private static final int[] POSITION_WEIGHTS = {
            120, -20, 20, 5, 5, 20, -20, 120,
            -20, -40, -5, -5, -5, -5, -40, -20,
            20, -5, 15, 3, 3, 15, -5, 20,
            5, -5, 3, 3, 3, 3, -5, 5,
            5, -5, 3, 3, 3, 3, -5, 5,
            20, -5, 15, 3, 3, 15, -5, 20,
            -20, -40, -5, -5, -5, -5, -40, -20,
            120, -20, 20, 5, 5, 20, -20, 120
    };

    public int evaluate(BoardState state) {
        var self = state.sideToMove();
        var opponent = self.opponent();

        var selfBoard = state.bits(self);
        var opponentBoard = state.bits(opponent);
        var empty = state.emptySquares();
        var occupied = state.occupiedCount();

        var discDiff = normalized(state.discCount(self), state.discCount(opponent));
        var mobility = normalized(Long.bitCount(state.legalMoves(self)), Long.bitCount(state.legalMoves(opponent)));
        var potentialMobility = normalized(
                Long.bitCount(BoardState.adjacent(opponentBoard) & empty),
                Long.bitCount(BoardState.adjacent(selfBoard) & empty)
        );
        var corners = 100 * (Long.bitCount(selfBoard & CORNERS) - Long.bitCount(opponentBoard & CORNERS));
        var frontier = -normalized(frontierCount(selfBoard, empty), frontierCount(opponentBoard, empty));
        var stability = 25 * (stableEdgeCount(selfBoard) - stableEdgeCount(opponentBoard));
        var positional = positionalScore(selfBoard, opponentBoard);
        var cornerPressure = cornerPressureScore(selfBoard, opponentBoard);
        var parity = (state.emptyCount() & 1) == 1 ? 1 : -1;

        int discWeight;
        int mobilityWeight;
        int potentialWeight;
        int frontierWeight;
        int positionalWeight;
        int parityWeight;

        if (occupied < 20) {
            discWeight = 5;
            mobilityWeight = 90;
            potentialWeight = 55;
            frontierWeight = 35;
            positionalWeight = 30;
            parityWeight = 5;
        } else if (occupied < 44) {
            discWeight = 20;
            mobilityWeight = 70;
            potentialWeight = 35;
            frontierWeight = 45;
            positionalWeight = 25;
            parityWeight = 8;
        } else {
            discWeight = 55;
            mobilityWeight = 35;
            potentialWeight = 20;
            frontierWeight = 35;
            positionalWeight = 15;
            parityWeight = 15;
        }

        return discWeight * discDiff
                + mobilityWeight * mobility
                + potentialWeight * potentialMobility
                + corners
                + frontierWeight * frontier
                + stability
                + positionalWeight * positional
                + cornerPressure
                + parityWeight * parity;
    }

    private static int frontierCount(long board, long empty) {
        return Long.bitCount(board & BoardState.adjacent(empty));
    }

    private static int positionalScore(long selfBoard, long opponentBoard) {
        int score = 0;
        long self = selfBoard;
        while (self != 0L) {
            var bit = Long.lowestOneBit(self);
            score += POSITION_WEIGHTS[Long.numberOfTrailingZeros(bit)];
            self ^= bit;
        }

        long opponent = opponentBoard;
        while (opponent != 0L) {
            var bit = Long.lowestOneBit(opponent);
            score -= POSITION_WEIGHTS[Long.numberOfTrailingZeros(bit)];
            opponent ^= bit;
        }

        return score;
    }

    private static int stableEdgeCount(long board) {
        long stable = 0L;
        if ((board & bit(0)) != 0L) {
            stable |= collectSequence(board, 0, 1, 8);
            stable |= collectSequence(board, 0, 8, 8);
        }
        if ((board & bit(7)) != 0L) {
            stable |= collectSequence(board, 7, -1, 8);
            stable |= collectSequence(board, 7, 8, 8);
        }
        if ((board & bit(56)) != 0L) {
            stable |= collectSequence(board, 56, 1, 8);
            stable |= collectSequence(board, 56, -8, 8);
        }
        if ((board & bit(63)) != 0L) {
            stable |= collectSequence(board, 63, -1, 8);
            stable |= collectSequence(board, 63, -8, 8);
        }
        return Long.bitCount(stable);
    }

    private static long collectSequence(long board, int start, int step, int length) {
        long collected = 0L;
        int index = start;
        for (int i = 0; i < length; i++, index += step) {
            if (index < 0 || index >= 64) {
                break;
            }
            var bit = bit(index);
            if ((board & bit) == 0L) {
                break;
            }
            collected |= bit;
        }
        return collected;
    }

    private static int cornerPressureScore(long selfBoard, long opponentBoard) {
        int score = 0;
        score += emptyCornerPenalty(0, 9, new int[]{1, 8}, selfBoard, opponentBoard);
        score += emptyCornerPenalty(7, 14, new int[]{6, 15}, selfBoard, opponentBoard);
        score += emptyCornerPenalty(56, 49, new int[]{48, 57}, selfBoard, opponentBoard);
        score += emptyCornerPenalty(63, 54, new int[]{55, 62}, selfBoard, opponentBoard);
        return score;
    }

    private static int emptyCornerPenalty(int corner, int xSquare, int[] cSquares, long selfBoard, long opponentBoard) {
        var cornerBit = bit(corner);
        if (((selfBoard | opponentBoard) & cornerBit) != 0L) {
            return 0;
        }

        int score = 0;
        if ((selfBoard & bit(xSquare)) != 0L) {
            score -= 90;
        }
        if ((opponentBoard & bit(xSquare)) != 0L) {
            score += 90;
        }
        for (var cSquare : cSquares) {
            if ((selfBoard & bit(cSquare)) != 0L) {
                score -= 35;
            }
            if ((opponentBoard & bit(cSquare)) != 0L) {
                score += 35;
            }
        }
        return score;
    }

    private static int normalized(int self, int opponent) {
        var denominator = self + opponent;
        if (denominator == 0) {
            return 0;
        }
        return 100 * (self - opponent) / denominator;
    }

    private static long bit(int index) {
        return 1L << index;
    }
}

