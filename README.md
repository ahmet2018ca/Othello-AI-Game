# Game-Theoretic Othello AI Engine

This project is a technical, command-line Othello engine written in Oracle Java 26. It was designed as a clean demonstration of classic game AI engineering: immutable state modeling, bitboard-based move generation, minimax-style adversarial search, alpha-beta pruning, heuristic evaluation, and late-game exact solving.

The result is a playable CLI game with two play modes:

- `Human vs Human` for local 1v1 play on the same machine
- `Human vs AI` with four difficulties: `Easy`, `Medium`, `Hard`, and `Godlike`

The strongest mode is intentionally difficult to beat. It searches deeply, orders moves aggressively, caches previously analyzed states, and becomes much more exact as the board approaches the endgame.

## Project Goals

This engine was built to feel like a serious systems-and-algorithms project rather than a toy implementation. The main goals were:

- build the full Othello ruleset cleanly around immutable board state
- make move generation fast enough to support deeper search
- create AI difficulty tiers that feel meaningfully different in play
- expose the whole system through a simple, usable CLI
- keep the code organized and readable so the design is easy to explain

## Core Features

- Oracle Java 26 implementation with straightforward build, run, and test scripts
- high-performance two-bitboard board representation using `long` values
- legal move generation and flip resolution across all eight directions
- minimax-style negamax search with alpha-beta pruning
- principal variation search style re-search for stronger pruning
- transposition table for caching previously explored positions
- killer-move and history-heuristic move ordering
- heuristic evaluator that changes emphasis over opening, midgame, and endgame
- exact late-game deepening when the number of empty squares becomes small enough
- CLI support for `Human vs Human`, `Human vs AI`, `hint`, `moves`, `undo`, and `quit`

## Project Layout

```text
src/main/java/com/othello
  app/        Application entrypoint
  ai/         Search engine, evaluation, transposition table, difficulty profiles
  cli/        Console renderer and interactive game controller
  engine/     Immutable board state and bitboard rule engine
  model/      Shared domain enums and records

src/test/java/com/othello/test
  OthelloTestRunner.java
```

## Prerequisites

You need Oracle JDK 26 installed.

Official Oracle Java 26 docs:
- [Java 26 Documentation](https://docs.oracle.com/en/java/javase/26/)

To confirm Java is available:

```powershell
java -version
javac -version
```

Expected output should show Java 26. This project was built around Oracle Java 26 and compiles with:

```powershell
javac --release 26
```

## Setup

1. Open a terminal in the project root:

```text
C:\CODES\RESUME__PROJECTS\OTHELLO__GAME\Othello-AI-Game
```

2. If you want to verify the toolchain first:

```powershell
java -version
javac -version
```

3. Build the project:

```powershell
.\build.ps1
```

If PowerShell execution policy blocks local scripts, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

You can also use the batch wrapper:

```bat
build.bat
```

## Running the Game

The simplest way to start the game is:

```powershell
.\run.ps1
```

If script execution is restricted:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

Or with the batch file:

```bat
run.bat
```

When the program launches, it opens a menu:

- `1. Human vs Human`
- `2. Human vs AI`
- `3. Rules and Controls`
- `4. Exit`

If you choose `Human vs AI`, the program then asks:

- which difficulty you want
- whether you want to play as Black or White

Black always moves first in Othello.

## How to Play

Each move is entered using board coordinates such as:

- `D3`
- `C4`
- `F5`
- `H8`

The board uses:

- `B` for black discs
- `W` for white discs
- `*` for legal moves
- `?` for the current hint square
- `.` for empty non-playable squares at that moment

During a game, you can use these commands:

- `hint`: asks the engine for its recommended move
- `moves`: prints the current legal move list
- `undo`: rewinds one ply
- `help`: prints the rules and controls
- `quit`: returns to the main menu

If a player has no legal move, the engine automatically performs a pass. The game ends when neither side can move, and the side with more discs wins.

## Example Play Flow

Typical session:

1. Launch the game with `.\run.ps1`
2. Choose `2` for `Human vs AI`
3. Choose a difficulty such as `4` for `Godlike`
4. Choose your color
5. Enter moves like `D3`, `C3`, `F6`
6. Use `hint` when you want engine analysis
7. Use `undo` if you want to step back one move
8. Finish the game and read the final score summary

## Building and Testing

Build main sources:

```powershell
.\build.ps1
```

Run smoke tests:

```powershell
.\test.ps1
```

Batch wrappers are also included:

```bat
test.bat
```

The smoke test runner checks:

- initial position legality
- opening flip correctness
- that every AI difficulty returns legal moves
- pass handling in a forced-pass position

## AI Architecture

The AI in this project is not a machine-learning model. It is a classical game-tree search engine. The difficulty levels are different search configurations over the same core engine rather than separately trained models.

At a high level, the engine works like this:

1. represent the board as two bitboards
2. generate all legal moves for the current side
3. recursively analyze future positions with negamax
4. prune branches with alpha-beta bounds
5. order strong candidate moves first to improve pruning
6. score non-terminal positions with a heuristic evaluator
7. switch into deeper or exact play as the endgame approaches

## Bitboard Representation

The board is stored using two 64-bit integers:

- one `long` for all black discs
- one `long` for all white discs

Each bit corresponds to one square on the 8x8 board. This representation is fast, compact, and very well suited to Othello because:

- occupancy checks become simple bit tests
- move generation becomes directional bit shifting
- flips can be resolved with low-level bitwise operations
- the state is small enough to copy safely for immutable search nodes

The rule engine uses directional flood-style scans and directional capture checks over all eight directions:

- north
- south
- east
- west
- northeast
- northwest
- southeast
- southwest

This allows legal moves and flips to be determined without storing a mutable 2D object graph.

## Search Engine

The search engine is implemented in a negamax formulation of minimax. Negamax is especially clean for two-player zero-sum games because the same logic can be reused for both sides by negating scores during recursion.

The search includes several important optimizations:

- `Alpha-beta pruning`
  Cuts off branches that cannot improve the current best line.

- `Iterative deepening`
  Searches depth 1, then 2, then 3, and so on until the configured limit or time budget is reached.

- `Principal variation search behavior`
  The first move is searched with a full window; later moves are often searched with a narrower window and only re-searched if they look promising.

- `Transposition table`
  Previously seen positions are cached with depth and bound metadata so the engine can reuse work.

- `Killer moves`
  Moves that caused cutoffs at a given ply are remembered and tried earlier in similar future nodes.

- `History heuristic`
  Moves that have historically performed well receive better ordering priority later.

- `Late-game exact deepening`
  When the number of empty squares is small enough, the engine increases search depth toward exact play instead of relying only on heuristics.

These optimizations are what allow the stronger difficulties to become genuinely dangerous.

## Evaluation Function

When the engine reaches a non-terminal node at the search horizon, it estimates the quality of the position using a weighted heuristic evaluation.

The evaluator blends:

- `Disc differential`
  Raw count difference between the current player and the opponent.

- `Mobility`
  How many legal moves each side currently has.

- `Potential mobility`
  How many empty squares sit adjacent to opponent discs and may become playable soon.

- `Corner occupancy`
  Corners are extremely valuable because they cannot be flipped once secured.

- `Frontier discs`
  Discs adjacent to empty squares are usually less stable and easier to attack.

- `Positional square weights`
  Stable and strategically important squares get positive scores, while risky squares near empty corners are penalized.

- `Corner pressure / X-square and C-square risk`
  Squares diagonally adjacent or edge-adjacent to an empty corner are often dangerous, so the evaluator penalizes taking them too early.

- `Parity`
  In many late-game situations, move parity matters, so the evaluator includes a parity signal based on remaining empties.

The weights are phase-aware. The engine emphasizes mobility and position more in the opening and midgame, then gradually increases the value of disc count and exact conversion as the board fills up.

## Difficulty Modes

The difficulty levels are implemented as search profiles, not separate engines.

- `Easy`
  Shallow tactical search with a short time budget and a randomness window. It can still find good moves, but it intentionally does not always choose the absolute best one.

- `Medium`
  Deeper search with better positional judgment and less randomness. This level starts to punish careless corner concessions and bad mobility decisions.

- `Hard`
  Serious search depth with strong move ordering and a more aggressive endgame transition. This level is designed to feel competitive.

- `Godlike`
  The strongest configuration in the project. It searches deeper, uses a longer time budget, and transitions into exact endgame solving more aggressively. It is intentionally difficult to outplay in a long strategic game.

## Design Decisions

Several design choices were made on purpose:

- `Immutable board state`
  Search code is easier to reason about when each move creates a fresh state instead of mutating shared objects.

- `Bitboards over arrays`
  Othello benefits heavily from compact bit-level operations, especially for move generation and flip resolution.

- `CLI instead of GUI`
  The command-line interface keeps the focus on engine quality, algorithms, and clean game flow.

- `Script-based build`
  The project currently uses PowerShell and batch wrappers so it can run immediately with just Oracle JDK 26 installed.

## Troubleshooting

- `PowerShell says scripts are disabled`
  Use:
  `powershell -ExecutionPolicy Bypass -File .\run.ps1`

- `java` or `javac` is not recognized
  Make sure Oracle JDK 26 is installed and added to `PATH`.

- `The game does not start from the right folder`
  Open your terminal inside the project root before running scripts.

- `I want to verify the installation`
  Run `.\build.ps1` and then `.\test.ps1`.

## Summary

This project is a game-theoretic Othello engine built to showcase strong Java fundamentals, object-oriented design, algorithmic thinking, and adversarial AI implementation. It combines clean domain modeling with a search-heavy engine that can scale from casual play to a genuinely punishing `Godlike` mode, all inside a lightweight CLI application.
