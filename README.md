# Game-Theoretic Othello AI Engine

A Java 26 command-line Othello engine built around immutable bitboards, minimax, alpha-beta pruning, transposition caching, and phase-aware evaluation. The project supports local 1v1 play and four AI difficulty tiers ranging from casual to intentionally oppressive.

## Highlights

- Oracle Java 26 command-line project with a clean package layout and Java 26 compilation scripts.
- Immutable two-bitboard board model with constant-time move generation and flips over all eight directions.
- Negamax search with alpha-beta pruning, principal variation search, killer moves, history heuristics, and a transposition table.
- Heuristic evaluator blending mobility, potential mobility, corners, frontier pressure, positional weights, parity, and corner-risk penalties.
- Exact late-game solving when the remaining empty squares are small enough.
- Interactive CLI with `Human vs Human`, `Human vs AI`, `hint`, `undo`, and automatic pass handling.

## Project Layout

```text
src/main/java/com/othello
  app/        Entrypoint
  ai/         Search engine, evaluator, and transposition table
  cli/        Console UI and game flow
  engine/     Bitboard-based board state and rule engine
  model/      Shared domain records and enums

src/test/java/com/othello/test
  OthelloTestRunner.java
```

## Requirements

- Oracle JDK 26

Oracle Java SE 26 docs:
- [Java 26 Documentation](https://docs.oracle.com/en/java/javase/26/)

## Running

PowerShell:

```powershell
./run.ps1
```

Batch:

```bat
run.bat
```

## Building

```powershell
./build.ps1
```

## Testing

```powershell
./test.ps1
```

## AI Difficulty Model

- `Easy`: shallow tactical search with controlled randomness so it still blunders.
- `Medium`: deeper positional search with lower randomness and better mobility play.
- `Hard`: serious alpha-beta search with stronger move ordering and exact endgame cutover.
- `Godlike`: the same engine pushed far deeper with a larger endgame solve window and much stricter move selection.

## Controls

- Enter moves in coordinate form like `D3`, `C4`, or `H8`.
- `hint` asks the engine for a recommended move.
- `moves` prints the legal move list.
- `undo` rewinds one ply.
- `quit` returns to the menu.

## Technical Notes

- The board uses one `long` for black discs and one `long` for white discs.
- Move generation floods rays outward from the current player bitboard and materializes legal targets from the resulting frontier.
- Applying a move computes directional captures and returns a brand-new immutable state, which keeps search code simple and safe.
- The AI uses depth-limited search in the opening and midgame, then upgrades to exact search near the end when the branching factor collapses.
