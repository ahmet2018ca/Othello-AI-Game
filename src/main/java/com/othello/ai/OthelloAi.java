package com.othello.ai;

import com.othello.engine.BoardState;
import com.othello.model.Difficulty;
import com.othello.model.Move;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.random.RandomGenerator;

public final class OthelloAi {
    private static final int WIN_SCORE = 1_000_000;
    private static final int NEGATIVE_INFINITY = Integer.MIN_VALUE / 4;
    private static final int POSITIVE_INFINITY = Integer.MAX_VALUE / 4;
    private static final long CORNERS = bit(0) | bit(7) | bit(56) | bit(63);
    private static final int[] ORDERING_WEIGHTS = {
            200, -80, 40, 20, 20, 40, -80, 200,
            -80, -140, -10, -10, -10, -10, -140, -80,
            40, -10, 35, 5, 5, 35, -10, 40,
            20, -10, 5, 5, 5, 5, -10, 20,
            20, -10, 5, 5, 5, 5, -10, 20,
            40, -10, 35, 5, 5, 35, -10, 40,
            -80, -140, -10, -10, -10, -10, -140, -80,
            200, -80, 40, 20, 20, 40, -80, 200
    };
    private static final SearchTimeoutException SEARCH_TIMEOUT = new SearchTimeoutException();

    private final Evaluator evaluator = new Evaluator();
    private final TranspositionTable transpositionTable = new TranspositionTable(1 << 20);
    private final RandomGenerator random = RandomGenerator.of("L64X128MixRandom");
    private final int[] killerOne = new int[64];
    private final int[] killerTwo = new int[64];
    private final int[][] history = new int[2][64];

    public OthelloAi() {
        Arrays.fill(killerOne, -1);
        Arrays.fill(killerTwo, -1);
    }

    public SearchOutcome chooseMove(BoardState state, Difficulty difficulty) {
        var legalMoves = state.legalMovesList();
        if (legalMoves.isEmpty()) {
            throw new IllegalArgumentException("AI cannot move in a state without legal moves");
        }

        if (legalMoves.size() == 1) {
            var forced = legalMoves.getFirst();
            return new SearchOutcome(forced, 0, 0, 1, 0, List.of(forced));
        }

        var profile = difficulty.profile();
        var start = System.nanoTime();
        var deadline = start + profile.timeLimitMillis() * 1_000_000L;

        Arrays.fill(killerOne, -1);
        Arrays.fill(killerTwo, -1);
        for (var scores : history) {
            Arrays.fill(scores, 0);
        }
        transpositionTable.nextGeneration();

        var frame = new SearchFrame(profile, deadline);
        SearchIteration bestCompleted = null;
        var maxDepth = Math.max(1, Math.min(profile.maxDepth(), state.emptyCount()));

        try {
            if (profile.iterativeDeepening()) {
                for (int depth = 1; depth <= maxDepth; depth++) {
                    bestCompleted = searchRoot(state, depth, frame);
                }
            } else {
                bestCompleted = searchRoot(state, maxDepth, frame);
            }
        } catch (SearchTimeoutException ignored) {
            if (bestCompleted == null) {
                bestCompleted = fallbackIteration(state);
            }
        }

        if (bestCompleted == null) {
            bestCompleted = fallbackIteration(state);
        }

        var selected = pickMove(bestCompleted.scoredMoves(), profile);
        var elapsedMillis = (System.nanoTime() - start) / 1_000_000L;
        var principalVariation = rebuildPrincipalVariation(state, selected.move(), bestCompleted.depth());

        return new SearchOutcome(
                selected.move(),
                selected.score(),
                bestCompleted.depth(),
                frame.nodes(),
                elapsedMillis,
                principalVariation
        );
    }

    private SearchIteration searchRoot(BoardState state, int depth, SearchFrame frame) {
        var moves = state.legalMovesList();
        var ttEntry = transpositionTable.probe(state);
        orderMoves(state, moves, 0, ttEntry == null ? -1 : ttEntry.bestMove());

        int alpha = NEGATIVE_INFINITY;
        int beta = POSITIVE_INFINITY;
        int bestScore = NEGATIVE_INFINITY;
        Move bestMove = moves.getFirst();
        var scoredMoves = new ArrayList<ScoredMove>(moves.size());

        for (int i = 0; i < moves.size(); i++) {
            var move = moves.get(i);
            var child = state.apply(move);
            final int score;

            if (i == 0) {
                score = -search(child, depth - 1, -beta, -alpha, 1, frame);
            } else {
                var probe = -search(child, depth - 1, -alpha - 1, -alpha, 1, frame);
                score = probe > alpha && probe < beta
                        ? -search(child, depth - 1, -beta, -alpha, 1, frame)
                        : probe;
            }

            scoredMoves.add(new ScoredMove(move, score));
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, score);
        }

        scoredMoves.sort(Comparator.comparingInt(ScoredMove::score).reversed());
        transpositionTable.store(state, depth, bestScore, TranspositionTable.EXACT, bestMove.index());
        return new SearchIteration(depth, bestMove, bestScore, List.copyOf(scoredMoves));
    }

    private int search(BoardState state, int depth, int alpha, int beta, int ply, SearchFrame frame) {
        frame.touch();
        if (state.isTerminal()) {
            return terminalScore(state, ply);
        }

        var empties = state.emptyCount();
        if (empties <= frame.profile().exactSolveEmptySquares() && depth < empties) {
            depth = empties;
        }
        if (depth == 0) {
            return evaluator.evaluate(state);
        }

        final int originalAlpha = alpha;
        final int originalBeta = beta;
        var ttEntry = transpositionTable.probe(state);
        if (ttEntry != null && ttEntry.depth() >= depth) {
            switch (ttEntry.flag()) {
                case TranspositionTable.EXACT -> {
                    return ttEntry.score();
                }
                case TranspositionTable.LOWER_BOUND -> alpha = Math.max(alpha, ttEntry.score());
                case TranspositionTable.UPPER_BOUND -> beta = Math.min(beta, ttEntry.score());
                default -> {
                }
            }
            if (alpha >= beta) {
                return ttEntry.score();
            }
        }

        var legalMask = state.legalMoves();
        if (legalMask == 0L) {
            var score = -search(state.pass(), depth, -beta, -alpha, ply + 1, frame);
            transpositionTable.store(state, depth, score, TranspositionTable.EXACT, -1);
            return score;
        }

        var moves = state.legalMovesList();
        orderMoves(state, moves, ply, ttEntry == null ? -1 : ttEntry.bestMove());

        int bestScore = NEGATIVE_INFINITY;
        int bestMove = moves.getFirst().index();

        for (int i = 0; i < moves.size(); i++) {
            var move = moves.get(i);
            var child = state.apply(move);
            final int score;

            if (i == 0) {
                score = -search(child, depth - 1, -beta, -alpha, ply + 1, frame);
            } else {
                var probe = -search(child, depth - 1, -alpha - 1, -alpha, ply + 1, frame);
                score = probe > alpha && probe < beta
                        ? -search(child, depth - 1, -beta, -alpha, ply + 1, frame)
                        : probe;
            }

            if (score > bestScore) {
                bestScore = score;
                bestMove = move.index();
            }
            if (score > alpha) {
                alpha = score;
            }
            if (alpha >= beta) {
                rememberKiller(state, move, ply, depth);
                break;
            }
        }

        var flag = bestScore <= originalAlpha
                ? TranspositionTable.UPPER_BOUND
                : bestScore >= originalBeta
                ? TranspositionTable.LOWER_BOUND
                : TranspositionTable.EXACT;
        transpositionTable.store(state, depth, bestScore, flag, bestMove);
        return bestScore;
    }

    private void orderMoves(BoardState state, List<Move> moves, int ply, int ttMove) {
        var scored = new int[moves.size()];
        for (int i = 0; i < moves.size(); i++) {
            var move = moves.get(i);
            int score = 0;
            if (move.index() == ttMove) {
                score += 2_000_000;
            }
            if ((move.bit() & CORNERS) != 0L) {
                score += 1_500_000;
            }
            if (move.index() == killerOne[ply]) {
                score += 750_000;
            }
            if (move.index() == killerTwo[ply]) {
                score += 500_000;
            }
            score += history[state.sideToMove().ordinal()][move.index()];
            score += ORDERING_WEIGHTS[move.index()] * 75;
            score += move.flipCount() * 120;
            scored[i] = score;
        }

        for (int i = 1; i < moves.size(); i++) {
            var move = moves.get(i);
            var score = scored[i];
            int j = i - 1;
            while (j >= 0 && scored[j] < score) {
                scored[j + 1] = scored[j];
                moves.set(j + 1, moves.get(j));
                j--;
            }
            scored[j + 1] = score;
            moves.set(j + 1, move);
        }
    }

    private void rememberKiller(BoardState state, Move move, int ply, int depth) {
        var index = move.index();
        if (killerOne[ply] != index) {
            killerTwo[ply] = killerOne[ply];
            killerOne[ply] = index;
        }
        history[state.sideToMove().ordinal()][index] += depth * depth;
    }

    private SearchIteration fallbackIteration(BoardState state) {
        var scoredMoves = new ArrayList<ScoredMove>();
        for (var move : state.legalMovesList()) {
            var score = -evaluator.evaluate(state.apply(move)) + ORDERING_WEIGHTS[move.index()] * 10;
            scoredMoves.add(new ScoredMove(move, score));
        }
        scoredMoves.sort(Comparator.comparingInt(ScoredMove::score).reversed());
        return new SearchIteration(0, scoredMoves.getFirst().move(), scoredMoves.getFirst().score(), List.copyOf(scoredMoves));
    }

    private ScoredMove pickMove(List<ScoredMove> scoredMoves, SearchProfile profile) {
        var best = scoredMoves.getFirst();
        if (profile.randomnessWindow() == 0 || scoredMoves.size() == 1) {
            return best;
        }

        var shortlisted = new ArrayList<ScoredMove>();
        var threshold = best.score() - profile.randomnessWindow();
        for (var scoredMove : scoredMoves) {
            if (shortlisted.size() >= profile.candidatePool()) {
                break;
            }
            if (scoredMove.score() >= threshold || shortlisted.isEmpty()) {
                shortlisted.add(scoredMove);
            }
        }
        return shortlisted.get(random.nextInt(shortlisted.size()));
    }

    private List<Move> rebuildPrincipalVariation(BoardState state, Move rootMove, int depth) {
        var pv = new ArrayList<Move>();
        pv.add(rootMove);
        var cursor = state.apply(rootMove);

        for (int remaining = depth - 1; remaining > 0 && !cursor.isTerminal(); remaining--) {
            var entry = transpositionTable.probe(cursor);
            if (entry == null || entry.bestMove() < 0) {
                break;
            }
            var next = cursor.findLegalMove(entry.bestMove());
            if (next == null) {
                break;
            }
            pv.add(next);
            cursor = cursor.apply(next);
        }
        return List.copyOf(pv);
    }

    private static int terminalScore(BoardState state, int ply) {
        var diff = state.discCount(state.sideToMove()) - state.discCount(state.sideToMove().opponent());
        if (diff == 0) {
            return 0;
        }
        return diff > 0
                ? WIN_SCORE + diff * 1_000 - ply
                : -WIN_SCORE + diff * 1_000 + ply;
    }

    private static long bit(int index) {
        return 1L << index;
    }

    private record ScoredMove(Move move, int score) {
    }

    private record SearchIteration(int depth, Move bestMove, int bestScore, List<ScoredMove> scoredMoves) {
    }

    private static final class SearchFrame {
        private final SearchProfile profile;
        private final long deadlineNanos;
        private long nodes;

        private SearchFrame(SearchProfile profile, long deadlineNanos) {
            this.profile = profile;
            this.deadlineNanos = deadlineNanos;
        }

        private SearchProfile profile() {
            return profile;
        }

        private long nodes() {
            return nodes;
        }

        private void touch() {
            nodes++;
            if ((nodes & 0x7FFL) == 0L && System.nanoTime() >= deadlineNanos) {
                throw SEARCH_TIMEOUT;
            }
        }
    }

    private static final class SearchTimeoutException extends RuntimeException {
        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
